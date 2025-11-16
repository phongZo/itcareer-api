package com.base.auth.controller;

import com.base.auth.constant.ITDreamConstant;
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
import com.base.auth.model.Task;
import com.base.auth.model.TaskQuestion;
import com.base.auth.model.criteria.TaskQuestionCriteria;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.StudentTaskQuestionProgressRepository;
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
    TaskQuestion taskQuestion = taskQuestionRepository.findByQuestionAndTaskId(createTaskQuestionForm.getQuestion(), createTaskQuestionForm.getTaskId()).orElse(null);
    // Nếu task question khác null thì cần kiểm tra thể loại và đã có câu hỏi nào trong task đã được tạo trước đó chưa
    // Những task question trong task sẽ gồm 2 trường hợp:
    // + Một là những task question được tạo với type nộp bằng file hoặc text
    // + Hai là những task question được tạo với type là trắc nghiệm
    if (taskQuestion != null){
      boolean validType = validateQuestionType(taskQuestion.getQuestionType(), createTaskQuestionForm.getQuestionType());
      if (!validType){
        throw new BadRequestException("Question cannot be created due to a type conflict", ErrorCode.TASK_QUESTION_ERROR_NOT_CREATE);
      }
      if (!Objects.equals(createTaskQuestionForm.getQuestionType(), ITDreamConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
        throw new BadRequestException("Task question already exist", ErrorCode.TASK_QUESTION_ERROR_EXIST);
      }
      boolean existTaskQuestion = taskQuestionRepository.existsByOptions(createTaskQuestionForm.getOptions());
      if (!existTaskQuestion){
        throw new BadRequestException("Task question already exist", ErrorCode.TASK_QUESTION_ERROR_EXIST);
      }
    }
    // Nếu task question = null thì cần kiểm tra tồn tại 1 dòng task question được tạo trước đó làm theo type nào
    // Nếu không có task question nào được tạo trước đó thì cho qua
    else{
      TaskQuestion firstQuestionInTask = taskQuestionRepository.findFirstByTaskId(createTaskQuestionForm.getTaskId());
      if (firstQuestionInTask != null){
        boolean validType = validateQuestionType(firstQuestionInTask.getQuestionType(), createTaskQuestionForm.getQuestionType());
        if (!validType){
          throw new BadRequestException("Question cannot be created due to a type conflict", ErrorCode.TASK_QUESTION_ERROR_NOT_CREATE);
        }
      }
      if (!Objects.equals(createTaskQuestionForm.getQuestionType(), ITDreamConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
        if (createTaskQuestionForm.getOptions() != null){
          throw new BadRequestException("Options cannot be created", ErrorCode.TASK_QUESTION_ERROR_NOT_CREATE_OPTION);
        }
      } else {
        if (createTaskQuestionForm.getOptions() == null){
          throw new BadRequestException("Options cannot be null", ErrorCode.TASK_QUESTION_ERROR_OPTION_NOT_NULL);
        }
      }
    }
    Task task = taskRepository.findById(createTaskQuestionForm.getTaskId()).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = task.getSimulation();
    if (simulation == null){
      throw new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND);
    }
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be created", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    taskQuestion = taskQuestionMapper.fromCreateTaskQuestionFormToEntity(createTaskQuestionForm);
    taskQuestion.setTask(task);
    taskQuestionRepository.save(taskQuestion);

    // Tự động set số câu trả lời sai bằng 1 nửa trong tổng số câu hỏi
    int currentTotalQuestion = task.getTotalQuestion() + 1;
    task.setTotalQuestion(currentTotalQuestion);
    if (Objects.equals(createTaskQuestionForm.getQuestionType(), ITDreamConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
      task.setMaxErrors((int) Math.ceil((double) currentTotalQuestion / 2));
    }
    taskRepository.save(task);

    if (Objects.equals(simulation.getStatus(), ITDreamConstant.STATUS_ACTIVE)){
      simulation.setStatus(ITDreamConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }
    apiMessageDto.setMessage("Create task question success");
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
    apiMessageDto.setMessage("Get list task question success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TQ_ST_L')")
  public ApiMessageDto<ResponseListDto<List<TaskQuestionStudentDto>>> getListForStudent(TaskQuestionCriteria taskQuestionCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<TaskQuestionStudentDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<TaskQuestionStudentDto>> responseListDto = new ResponseListDto<>();
    taskQuestionCriteria.setStatus(ITDreamConstant.STATUS_ACTIVE);
    Page<TaskQuestion> taskQuestions = taskQuestionRepository.findAll(taskQuestionCriteria.getSpecification(), pageable);
    responseListDto.setContent(taskQuestionMapper.fromEntityToTaskQuestionStudentDtoList(taskQuestions.getContent()));
    responseListDto.setTotalElements(taskQuestions.getTotalElements());
    responseListDto.setTotalPages(taskQuestions.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list task question success");
    return apiMessageDto;
  }

  @GetMapping(value = "/educator-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TQ_ED_L')")
  public ApiMessageDto<ResponseListDto<List<TaskQuestionEducatorDto>>> getListForEducator(TaskQuestionCriteria taskQuestionCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<TaskQuestionEducatorDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<TaskQuestionEducatorDto>> responseListDto = new ResponseListDto<>();
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
    if (!Objects.equals(taskQuestion.getQuestion(), updateQuestionTaskForm.getQuestion())){
      if (Objects.equals(taskQuestion.getQuestionType(), ITDreamConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
        if (updateQuestionTaskForm.getOptions() == null){
          throw new BadRequestException("Options cannot be null", ErrorCode.TASK_QUESTION_ERROR_OPTION_NOT_NULL);
        } else {
          boolean existTaskQuestion = taskQuestionRepository.existsByQuestionAndOptionsAndTaskId(updateQuestionTaskForm.getQuestion(), updateQuestionTaskForm.getOptions(), taskQuestion.getTask().getId());
          if (existTaskQuestion){
            throw new BadRequestException("Task question already exist", ErrorCode.TASK_QUESTION_ERROR_EXIST);
          }
        }
      } else {
        if (updateQuestionTaskForm.getOptions() != null){
          throw new BadRequestException("Question cannot be updated", ErrorCode.TASK_QUESTION_ERROR_NOT_UPDATE);
        } else {
          boolean existTaskQuestion = taskQuestionRepository.existsByQuestionAndTaskId(updateQuestionTaskForm.getQuestion(), taskQuestion.getTask().getId());
          if (existTaskQuestion){
            throw new BadRequestException("Task question already exist", ErrorCode.TASK_QUESTION_ERROR_EXIST);
          }
        }
      }
    }

    Task task = taskQuestion.getTask();
    if (task == null){
      throw new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND);
    }
    Simulation simulation = task.getSimulation();
    if (simulation == null){
      throw new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND);
    }

    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be updated", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    taskQuestionMapper.fromUpdateTaskQuestionFormToEntity(updateQuestionTaskForm, taskQuestion);
    taskQuestionRepository.save(taskQuestion);

    if (Objects.equals(simulation.getStatus(), ITDreamConstant.STATUS_ACTIVE)){
      simulation.setStatus(ITDreamConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }
    apiMessageDto.setMessage("Update task question success");
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
    Task task = taskQuestion.getTask();
    if (task == null){
      throw new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND);
    }
    Simulation simulation = task.getSimulation();
    if (simulation == null){
      throw new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND);
    }

    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be deleted", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }

    int currentTotalQuestion = task.getTotalQuestion() - 1;
    task.setTotalQuestion(currentTotalQuestion);
    if (Objects.equals(taskQuestion.getQuestionType(), ITDreamConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
      task.setMaxErrors((int) Math.ceil((double) currentTotalQuestion / 2));
    }
    taskRepository.save(task);

    studentTaskQuestionProgressRepository.deleteAllByTaskQuestionId(id);
    taskQuestionRepository.delete(taskQuestion);

    if (Objects.equals(simulation.getStatus(), ITDreamConstant.STATUS_ACTIVE)){
      simulation.setStatus(ITDreamConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }
    apiMessageDto.setMessage("Delete success");
    return apiMessageDto;
  }

  private boolean validateQuestionType(Integer taskQuestionType, Integer createTaskQuestionType){
    if ((Objects.equals(taskQuestionType, ITDreamConstant.QUESTION_TYPE_FILE)
        || Objects.equals(taskQuestionType, ITDreamConstant.QUESTION_TYPE_TEXT))
        && Objects.equals(createTaskQuestionType, ITDreamConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
      return false;
    } else if (Objects.equals(taskQuestionType, ITDreamConstant.QUESTION_TYPE_MULTIPLE_CHOICE)
        && (Objects.equals(createTaskQuestionType, ITDreamConstant.QUESTION_TYPE_FILE)
        || Objects.equals(createTaskQuestionType, ITDreamConstant.QUESTION_TYPE_TEXT))){
      return false;
    }
    return true;
  }
}
