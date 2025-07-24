package com.base.auth.controller;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.subtask.SubTaskClientDto;
import com.base.auth.dto.subtask.SubTaskDisplayDto;
import com.base.auth.dto.subtask.SubTaskDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.subtask.CreateSubTaskForm;
import com.base.auth.form.subtask.UpdateSubTaskForm;
import com.base.auth.mapper.SubTaskMapper;
import com.base.auth.model.Simulation;
import com.base.auth.model.SubTask;
import com.base.auth.model.Task;
import com.base.auth.model.criteria.SubTaskCriteria;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.SubTaskRepository;
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
@RequestMapping("/v1/subtask")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class SubTaskController extends ABasicController{
  @Autowired
  SubTaskRepository subTaskRepository;

  @Autowired
  SubTaskMapper subTaskMapper;

  @Autowired
  TaskRepository taskRepository;

  @Autowired
  SimulationRepository simulationRepository;

  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STA_C')")
  public ApiMessageDto<String> create(@Valid @RequestBody CreateSubTaskForm createSubTaskForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Task task = taskRepository.findById(createSubTaskForm.getTaskId()).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    SubTask subTask = subTaskRepository.findByTitleAndTaskId(createSubTaskForm.getTitle(), createSubTaskForm.getTaskId()).orElse(null);
    if (subTask != null){
      throw new BadRequestException("Subtask title already exist", ErrorCode.SUBTASK_ERROR_EXIST);
    }
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));

    subTask = subTaskMapper.fromCreateSubTaskFormToEntity(createSubTaskForm);
    subTask.setTask(task);
    subTaskRepository.save(subTask);
    if (Objects.equals(UserBaseConstant.STATUS_ACTIVE, simulation.getStatus())){
      simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }
    apiMessageDto.setMessage("Create subtask success");
    return apiMessageDto;
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STA_L')")
  public ApiMessageDto<ResponseListDto<List<SubTaskDto>>> getList(@Valid SubTaskCriteria subTaskCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<SubTaskDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<SubTaskDto>> responseListDto = new ResponseListDto<>();
    Task task = taskRepository.findById(subTaskCriteria.getTaskId()).orElseThrow(()
        -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(subTaskCriteria.getSimulationId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    Page<SubTask> subTasks = subTaskRepository.findAll(subTaskCriteria.getSpecification(), pageable);
    responseListDto.setContent(subTaskMapper.fromEntityToSubTaskDtoList(subTasks.getContent()));
    responseListDto.setTotalElements(subTasks.getTotalElements());
    responseListDto.setTotalPages(subTasks.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STA_ST_L')")
  public ApiMessageDto<ResponseListDto<List<SubTaskDisplayDto>>> getListForStudent(@Valid SubTaskCriteria subTaskCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<SubTaskDisplayDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<SubTaskDisplayDto>> responseListDto = new ResponseListDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    Task task = taskRepository.findById(subTaskCriteria.getTaskId()).orElseThrow(()
        -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(subTaskCriteria.getSimulationId()).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    subTaskCriteria.setStatus(UserBaseConstant.STATUS_ACTIVE);
    Page<SubTask> subTasks = subTaskRepository.findAll(subTaskCriteria.getSpecification(), pageable);
    responseListDto.setContent(subTaskMapper.fromEntityToSubTaskDisplayDtoList(subTasks.getContent()));
    responseListDto.setTotalElements(subTasks.getTotalElements());
    responseListDto.setTotalPages(subTasks.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/educator-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STA_ED_L')")
  public ApiMessageDto<ResponseListDto<List<SubTaskDisplayDto>>> getListForEducator(@Valid SubTaskCriteria subTaskCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<SubTaskDisplayDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<SubTaskDisplayDto>> responseListDto = new ResponseListDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Task task = taskRepository.findById(subTaskCriteria.getTaskId()).orElseThrow(()
        -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(subTaskCriteria.getSimulationId()).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    subTaskCriteria.setEducatorId(getCurrentUser());
    Page<SubTask> subTasks = subTaskRepository.findAll(subTaskCriteria.getSpecification(), pageable);
    responseListDto.setContent(subTaskMapper.fromEntityToSubTaskDisplayDtoList(subTasks.getContent()));
    responseListDto.setTotalElements(subTasks.getTotalElements());
    responseListDto.setTotalPages(subTasks.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STA_V')")
  public ApiMessageDto<SubTaskDto> get(@PathVariable("id") Long id){
    ApiMessageDto<SubTaskDto> apiMessageDto = new ApiMessageDto<>();
    SubTask subTask = subTaskRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    Task task = taskRepository.findById(subTask.getTask().getId()).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    SubTaskDto subTaskDto = subTaskMapper.fromEntityToSubTaskDto(subTask);
    apiMessageDto.setData(subTaskDto);
    apiMessageDto.setMessage("Get success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STA_ST_V')")
  public ApiMessageDto<SubTaskClientDto> getForStudent(@PathVariable("id") Long id){
    ApiMessageDto<SubTaskClientDto> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    SubTask subTask = subTaskRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    Task task = taskRepository.findById(subTask.getTask().getId()).orElseThrow(()
        -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getStatus(), UserBaseConstant.STATUS_ACTIVE)){
      throw new BadRequestException("Simulation not active", ErrorCode.SIMULATION_ERROR_NOT_ACTIVE);
    }
    SubTaskClientDto subTaskDto = subTaskMapper.fromEntityToSubTaskClientDto(subTask);
    apiMessageDto.setData(subTaskDto);
    apiMessageDto.setMessage("Get success");
    return apiMessageDto;
  }

  @GetMapping(value = "/educator-get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STA_ED_V')")
  public ApiMessageDto<SubTaskClientDto> getForEducator(@PathVariable("id") Long id){
    ApiMessageDto<SubTaskClientDto> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    SubTask subTask = subTaskRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    Task task = taskRepository.findById(subTask.getTask().getId()).orElseThrow(()
        -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Educator cannot get simulation", ErrorCode.SIMULATION_ERROR_NOT_GET);
    }
    SubTaskClientDto subTaskDto = subTaskMapper.fromEntityToSubTaskClientDto(subTask);
    apiMessageDto.setData(subTaskDto);
    apiMessageDto.setMessage("Get success");
    return apiMessageDto;
  }

  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STA_U')")
  public ApiMessageDto<String> update(@Valid @RequestBody UpdateSubTaskForm updateSubTaskForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    SubTask subTask = subTaskRepository.findById(updateSubTaskForm.getId()).orElseThrow(()
    -> new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    if (!Objects.equals(subTask.getTitle(), updateSubTaskForm.getTitle())){
      SubTask existSubTask = subTaskRepository.findByTitleAndTaskId(updateSubTaskForm.getTitle(), subTask.getTask().getId()).orElse(null);
      if (existSubTask != null){
        throw new BadRequestException("Subtask title already exist", ErrorCode.SUBTASK_ERROR_EXIST);
      }
    }
    Task task = taskRepository.findById(updateSubTaskForm.getTaskId()).orElseThrow(()
        -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be updated", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    subTaskMapper.fromUpdateSubTaskFormToEntity(updateSubTaskForm, subTask);
    subTask.setTask(task);
    subTaskRepository.save(subTask);
    if (Objects.equals(UserBaseConstant.STATUS_ACTIVE, simulation.getStatus())){
      simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }

    apiMessageDto.setMessage("Update success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STA_D')")
  public ApiMessageDto<String> delete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    SubTask subTask = subTaskRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Subtask not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Task task = taskRepository.findById(subTask.getTask().getId()).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findById(task.getSimulation().getId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be deleted. Because the educator is not correct", ErrorCode.SIMULATION_ERROR_NOT_DELETE);
    }
    subTaskRepository.delete(subTask);
    if (Objects.equals(simulation.getStatus(), UserBaseConstant.STATUS_ACTIVE)){
      simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }
    apiMessageDto.setMessage("Delete success");
    return apiMessageDto;
  }
}
