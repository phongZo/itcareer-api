package com.base.auth.controller;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.taskQuestion.TaskQuestionDto;
import com.base.auth.dto.taskQuestion.TaskQuestionEducatorDto;
import com.base.auth.dto.taskQuestion.TaskQuestionStudentDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.taskQuestion.CreateTaskQuestionForm;
import com.base.auth.form.taskQuestion.UpdateQuestionTaskForm;
import com.base.auth.mapper.TaskQuestionMapper;
import com.base.auth.model.Simulation;
import com.base.auth.model.StudentTaskQuestionProgress;
import com.base.auth.model.SubTask;
import com.base.auth.model.Task;
import com.base.auth.model.TaskQuestion;
import com.base.auth.model.criteria.TaskQuestionCriteria;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.StudentTaskQuestionProgressRepository;
import com.base.auth.repository.SubTaskRepository;
import com.base.auth.repository.TaskQuestionRepository;
import com.base.auth.repository.TaskRepository;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/task-question")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class TaskQuestionController extends ABasicController{
  @Autowired
  TaskQuestionRepository taskQuestionRepository;

  @Autowired
  TaskQuestionMapper taskQuestionMapper;

  @Autowired
  SubTaskRepository subTaskRepository;

  @Autowired
  TaskRepository taskRepository;

  @Autowired
  SimulationRepository simulationRepository;

  @Autowired
  StudentTaskQuestionProgressRepository studentTaskQuestionProgressRepository;

  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TQ_C')")
  public ApiMessageDto<String> create(@Valid @RequestBody CreateTaskQuestionForm createTaskQuestionForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    TaskQuestion taskQuestion = taskQuestionRepository.findByQuestionAndSubTaskId(createTaskQuestionForm.getQuestion(), createTaskQuestionForm.getSubTaskId()).orElse(null);
    if (taskQuestion != null){
      if (!validateQuestionType(taskQuestion.getQuestionType(), createTaskQuestionForm.getQuestionType())){
        throw new BadRequestException("Question cannot be created due to a type conflict", ErrorCode.TASK_QUESTION_ERROR_NOT_CREATE);
      }
      if (!Objects.equals(createTaskQuestionForm.getQuestionType(), UserBaseConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
        if (createTaskQuestionForm.getOptions() != null){
          throw new BadRequestException("Options cannot be created", ErrorCode.TASK_QUESTION_ERROR_NOT_CREATE_OPTION);
        }
        throw new BadRequestException("Task question already exist", ErrorCode.TASK_QUESTION_ERROR_EXIST);
      } else {
        TaskQuestion existTaskQuestion = taskQuestionRepository.findByOptions(createTaskQuestionForm.getOptions()).orElse(null);
        if (existTaskQuestion != null){
          throw new BadRequestException("Task question already exist", ErrorCode.TASK_QUESTION_ERROR_EXIST);
        }
      }
    } else{
      TaskQuestion existTaskQuestionBySubTaskId = taskQuestionRepository.findFirstBySubTaskId(createTaskQuestionForm.getSubTaskId());
      if (existTaskQuestionBySubTaskId != null && !validateQuestionType(existTaskQuestionBySubTaskId.getQuestionType(), createTaskQuestionForm.getQuestionType())){
        throw new BadRequestException("Question cannot be created due to a type conflict", ErrorCode.TASK_QUESTION_ERROR_NOT_CREATE);
      } else{
        if (!Objects.equals(createTaskQuestionForm.getQuestionType(), UserBaseConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
          if (createTaskQuestionForm.getOptions() != null){
            throw new BadRequestException("Options cannot be created", ErrorCode.TASK_QUESTION_ERROR_NOT_CREATE_OPTION);
          }
        } else {
          if (createTaskQuestionForm.getOptions() == null){
            throw new BadRequestException("Options cannot be null", ErrorCode.TASK_QUESTION_ERROR_OPTION_NOT_NULL);
          }
        }
      }
    }
    SubTask subTask = subTaskRepository.findById(createTaskQuestionForm.getSubTaskId()).orElseThrow(()
    -> new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    Task task = taskRepository.findById(subTask.getTask().getId()).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));

    taskQuestion = taskQuestionMapper.fromCreateTaskQuestionFormToEntity(createTaskQuestionForm);
    taskQuestion.setSubTask(subTask);
    taskQuestionRepository.save(taskQuestion);

    if (Objects.equals(createTaskQuestionForm.getQuestionType(), UserBaseConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
      int currentTotalQuestion = subTask.getTotalQuestion() + 1;
      subTask.setTotalQuestion(currentTotalQuestion);
      subTask.setMaxErrors((int) Math.ceil((double) currentTotalQuestion / 2));
      subTaskRepository.save(subTask);
    }

    if (Objects.equals(simulation.getStatus(), UserBaseConstant.STATUS_ACTIVE)){
      simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }

    apiMessageDto.setMessage("Create success");
    return apiMessageDto;
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TQ_L')")
  public ApiMessageDto<ResponseListDto<List<TaskQuestionDto>>> getList(TaskQuestionCriteria taskQuestionCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<TaskQuestionDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<TaskQuestionDto>> responseListDto = new ResponseListDto<>();
    Page<TaskQuestion> taskQuestions = taskQuestionRepository.findAll(taskQuestionCriteria.getSpecification(), pageable);
    responseListDto.setContent(taskQuestionMapper.fromEntityToTaskQuestionDtoList(taskQuestions.getContent()));
    responseListDto.setTotalElements(taskQuestions.getTotalElements());
    responseListDto.setTotalPages(taskQuestions.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TQ_ST_L')")
  public ApiMessageDto<ResponseListDto<List<TaskQuestionStudentDto>>> getListForStudent(TaskQuestionCriteria taskQuestionCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<TaskQuestionStudentDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<TaskQuestionStudentDto>> responseListDto = new ResponseListDto<>();
    taskQuestionCriteria.setStatus(UserBaseConstant.STATUS_ACTIVE);
    Page<TaskQuestion> taskQuestions = taskQuestionRepository.findAll(taskQuestionCriteria.getSpecification(), pageable);
    responseListDto.setContent(taskQuestionMapper.fromEntityToTaskQuestionStudentDtoList(taskQuestions.getContent()));
    responseListDto.setTotalElements(taskQuestions.getTotalElements());
    responseListDto.setTotalPages(taskQuestions.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/educator-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TQ_ED_L')")
  public ApiMessageDto<ResponseListDto<List<TaskQuestionEducatorDto>>> getListForEducator(TaskQuestionCriteria taskQuestionCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<TaskQuestionEducatorDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<TaskQuestionEducatorDto>> responseListDto = new ResponseListDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    taskQuestionCriteria.setEducatorId(getCurrentUser());
    Page<TaskQuestion> taskQuestions = taskQuestionRepository.findAll(taskQuestionCriteria.getSpecification(), pageable);
    responseListDto.setContent(taskQuestionMapper.fromEntityToTaskQuestionEducatorDtoList(taskQuestions.getContent()));
    responseListDto.setTotalElements(taskQuestions.getTotalElements());
    responseListDto.setTotalPages(taskQuestions.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TQ_U')")
  public ApiMessageDto<String> update(@Valid @RequestBody UpdateQuestionTaskForm updateQuestionTaskForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    TaskQuestion taskQuestion = taskQuestionRepository.findById(updateQuestionTaskForm.getId()).orElseThrow(()
    -> new NotFoundException("Task question not found", ErrorCode.TASK_QUESTION_ERROR_NOT_FOUND));
    if (!Objects.equals(taskQuestion.getQuestion(), updateQuestionTaskForm.getQuestion()) && Objects.equals(taskQuestion.getSubTask().getId(), updateQuestionTaskForm.getSubtaskId())){
      if (Objects.equals(taskQuestion.getQuestionType(), UserBaseConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
        if (updateQuestionTaskForm.getOptions() == null){
          throw new BadRequestException("Options cannot be null", ErrorCode.TASK_QUESTION_ERROR_OPTION_NOT_NULL);
        } else {
          TaskQuestion existTaskQuestion = taskQuestionRepository.findByQuestionAndOptions(updateQuestionTaskForm.getQuestion(), updateQuestionTaskForm.getOptions()).orElse(null);
          if (existTaskQuestion != null){
            throw new BadRequestException("Task question already exist", ErrorCode.TASK_QUESTION_ERROR_EXIST);
          }
        }
      } else {
        if (updateQuestionTaskForm.getOptions() != null){
          throw new BadRequestException("Question cannot be updated", ErrorCode.TASK_QUESTION_ERROR_NOT_UPDATE);
        } else {
          TaskQuestion existTaskQuestion = taskQuestionRepository.findByQuestionAndSubTaskId(updateQuestionTaskForm.getQuestion(), updateQuestionTaskForm.getSubtaskId()).orElse(null);
          if (existTaskQuestion != null){
            throw new BadRequestException("Task question already exist", ErrorCode.TASK_QUESTION_ERROR_EXIST);
          }
        }
      }
    } else if (!Objects.equals(taskQuestion.getSubTask().getId(), updateQuestionTaskForm.getSubtaskId())) {
      TaskQuestion existTaskQuestionBySubTaskId = taskQuestionRepository.findFirstBySubTaskId(updateQuestionTaskForm.getSubtaskId());
      if (existTaskQuestionBySubTaskId != null && !validateQuestionType(taskQuestion.getQuestionType(), existTaskQuestionBySubTaskId.getQuestionType())){
        throw new BadRequestException("Question cannot be created due to a type conflict", ErrorCode.TASK_QUESTION_ERROR_NOT_CREATE);
      }
    }
    SubTask subTask = subTaskRepository.findById(updateQuestionTaskForm.getSubtaskId()).orElseThrow(()
        -> new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    Task task = taskRepository.findById(subTask.getTask().getId()).orElseThrow(()
        -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be updated", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    taskQuestionMapper.fromUpdateTaskQuestionFormToEntity(updateQuestionTaskForm, taskQuestion);
    taskQuestionRepository.save(taskQuestion);

    if (Objects.equals(simulation.getStatus(), UserBaseConstant.STATUS_ACTIVE)){
      simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }

    apiMessageDto.setMessage("Update success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TQ_D')")
  public ApiMessageDto<String> delete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    TaskQuestion taskQuestion = taskQuestionRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Task question not found", ErrorCode.TASK_QUESTION_ERROR_NOT_FOUND));
    SubTask subTask = subTaskRepository.findById(taskQuestion.getSubTask().getId()).orElseThrow(()
        -> new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    Task task = taskRepository.findById(subTask.getTask().getId()).orElseThrow(()
        -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be deleted", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }

    if (Objects.equals(taskQuestion.getQuestionType(), UserBaseConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
      int currentTotalQuestion = subTask.getTotalQuestion() - 1;
      subTask.setTotalQuestion(currentTotalQuestion);
      subTask.setMaxErrors((int) Math.ceil((double) currentTotalQuestion / 2));
      subTaskRepository.save(subTask);
    }

    StudentTaskQuestionProgress studentTaskQuestionProgress = studentTaskQuestionProgressRepository.findFirstByTaskQuestionId(id).orElse(null);
    if (studentTaskQuestionProgress != null){
      throw new BadRequestException("Task question cannot be deleted", ErrorCode.TASK_ERROR_NOT_DELETE);
    }

    taskQuestionRepository.delete(taskQuestion);


    if (Objects.equals(simulation.getStatus(), UserBaseConstant.STATUS_ACTIVE)){
      simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }
    apiMessageDto.setMessage("Delete success");
    return apiMessageDto;
  }

  private Boolean validateQuestionType(Integer taskQuestionType, Integer createTaskQuestionType){
    if ((Objects.equals(taskQuestionType, UserBaseConstant.QUESTION_TYPE_FILE)
        || Objects.equals(taskQuestionType, UserBaseConstant.QUESTION_TYPE_TEXT))
        && Objects.equals(createTaskQuestionType, UserBaseConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
      return false;
    } else if (Objects.equals(taskQuestionType, UserBaseConstant.QUESTION_TYPE_MULTIPLE_CHOICE)
        && (Objects.equals(createTaskQuestionType, UserBaseConstant.QUESTION_TYPE_FILE)
        || Objects.equals(createTaskQuestionType, UserBaseConstant.QUESTION_TYPE_TEXT))){
      return false;
    }
    return true;
  }
}
