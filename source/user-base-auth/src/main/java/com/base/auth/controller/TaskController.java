package com.base.auth.controller;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.task.TaskDisplayDto;
import com.base.auth.dto.task.TaskDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.task.CreateTaskForm;
import com.base.auth.form.task.UpdateTaskForm;
import com.base.auth.mapper.TaskMapper;
import com.base.auth.model.Simulation;
import com.base.auth.model.SubTask;
import com.base.auth.model.Task;
import com.base.auth.model.criteria.TaskCriteria;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.SubTaskRepository;
import com.base.auth.repository.TaskQuestionRepository;
import com.base.auth.repository.TaskRepository;
import java.util.List;
import java.util.Objects;
import javax.transaction.Transactional;
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
@RequestMapping("/v1/task")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class TaskController extends ABasicController{
  @Autowired
  TaskRepository taskRepository;

  @Autowired
  SimulationRepository simulationRepository;

  @Autowired
  TaskMapper taskMapper;

  @Autowired
  SubTaskRepository subTaskRepository;

  @Autowired
  TaskQuestionRepository taskQuestionRepository;

  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_C')")
  public ApiMessageDto<String> create(@Valid @RequestBody CreateTaskForm createTaskForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Simulation simulation = simulationRepository.findById(createTaskForm.getSimulationId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    Task task = taskRepository.findByNameAndSimulationId(createTaskForm.getName(), createTaskForm.getSimulationId()).orElse(null);
    if (task != null){
      throw new BadRequestException("Task name already exist", ErrorCode.TASK_ERROR_EXIST);
    }

    task = taskMapper.fromCreateTaskFormToEntity(createTaskForm);
    task.setSimulation(simulation);
    taskRepository.save(task);

    if (Objects.equals(UserBaseConstant.STATUS_ACTIVE, simulation.getStatus())){
      simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }

    apiMessageDto.setMessage("Create task success");
    return apiMessageDto;
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_L')")
  public ApiMessageDto<ResponseListDto<List<TaskDto>>> getList(@Valid TaskCriteria taskCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<TaskDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<TaskDto>> responseListDto = new ResponseListDto<>();
    Page<Task> tasks = taskRepository.findAll(taskCriteria.getSpecification(), pageable);
    responseListDto.setContent(taskMapper.fromEntityToTaskDtoList(tasks.getContent()));
    responseListDto.setTotalElements(tasks.getTotalElements());
    responseListDto.setTotalPages(tasks.getTotalPages());

    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_ST_L')")
  public ApiMessageDto<ResponseListDto<List<TaskDisplayDto>>> getListForStudent(@Valid TaskCriteria taskCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<TaskDisplayDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<TaskDisplayDto>> responseListDto = new ResponseListDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    taskCriteria.setStatus(UserBaseConstant.STATUS_ACTIVE);
    Page<Task> tasks = taskRepository.findAll(taskCriteria.getSpecification(), pageable);
    responseListDto.setContent(taskMapper.fromEntityToTaskDisplayDtoList(tasks.getContent()));
    responseListDto.setTotalElements(tasks.getTotalElements());
    responseListDto.setTotalPages(tasks.getTotalPages());

    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "educator-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_ED_L')")
  public ApiMessageDto<ResponseListDto<List<TaskDisplayDto>>> getListForEducator(@Valid TaskCriteria taskCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<TaskDisplayDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<TaskDisplayDto>> responseListDto = new ResponseListDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    taskCriteria.setEducatorId(getCurrentUser());
    Page<Task> tasks = taskRepository.findAll(taskCriteria.getSpecification(), pageable);
    responseListDto.setContent(taskMapper.fromEntityToTaskDisplayDtoList(tasks.getContent()));
    responseListDto.setTotalElements(tasks.getTotalElements());
    responseListDto.setTotalPages(tasks.getTotalPages());

    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_U')")
  public ApiMessageDto<String> update(@Valid @RequestBody UpdateTaskForm updateTaskForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Task task = taskRepository.findById(updateTaskForm.getId()).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    if (!Objects.equals(updateTaskForm.getName(), task.getName())){
      Task existTask = taskRepository.findByNameAndSimulationId(updateTaskForm.getName(), task.getSimulation().getId()).orElse(null);
      if (existTask != null){
        throw new BadRequestException("Task name already exist", ErrorCode.TASK_ERROR_EXIST);
      }
    }
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be updated", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }

    taskMapper.fromUpdateTaskFormToEntity(updateTaskForm, task);
    taskRepository.save(task);

    if (Objects.equals(UserBaseConstant.STATUS_ACTIVE, task.getSimulation().getStatus())){
      simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }

    apiMessageDto.setMessage("Update task success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_D')")
  @Transactional
  public ApiMessageDto<String> delete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Task task = taskRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be deleted", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    taskQuestionRepository.deleteAllTaskQuestionByTaskId(id);
    subTaskRepository.deleteByTaskId(id);
    taskRepository.delete(task);
    if (Objects.equals(simulation.getStatus(), UserBaseConstant.STATUS_ACTIVE)){
      simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }
    apiMessageDto.setMessage("Delete task success");
    return apiMessageDto;
  }
}
