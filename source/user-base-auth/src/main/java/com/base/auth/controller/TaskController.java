package com.base.auth.controller;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.task.TaskDisplayDto;
import com.base.auth.dto.task.TaskDto;
import com.base.auth.dto.task.TaskEducatorDto;
import com.base.auth.dto.task.TaskStudentDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.task.CreateTaskForm;
import com.base.auth.form.RequestProcessVideoMessageForm;
import com.base.auth.form.task.UpdateTaskForm;
import com.base.auth.mapper.TaskMapper;
import com.base.auth.model.Simulation;
import com.base.auth.model.Task;
import com.base.auth.model.criteria.TaskCriteria;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.StudentSubTaskProgressRepository;
import com.base.auth.repository.StudentTaskQuestionProgressRepository;
import com.base.auth.repository.TaskQuestionRepository;
import com.base.auth.repository.TaskRepository;
import com.base.auth.service.ProcessVideoService;
import java.util.List;
import java.util.Objects;
import javax.transaction.Transactional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
  TaskQuestionRepository taskQuestionRepository;

  @Autowired
  StudentSubTaskProgressRepository studentSubTaskProgressRepository;

  @Autowired
  StudentTaskQuestionProgressRepository studentTaskQuestionProgressRepository;

  @Autowired
  ProcessVideoService processVideoService;

  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_C')")
  public ApiMessageDto<String> create(@Valid @RequestBody CreateTaskForm createTaskForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Simulation simulation = simulationRepository.findById(createTaskForm.getSimulationId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Task in simulation cannot be created", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    if (!Objects.equals(simulation.getStatus(), ITDreamConstant.STATUS_WAITING_APPROVE) && !Objects.equals(simulation.getStatus(), ITDreamConstant.STATUS_ACTIVE)){
      throw new BadRequestException("Simulation cannot be activated or is awaiting approval", ErrorCode.SIMULATION_ERROR_NOT_ACTIVE);
    }

    if (Objects.equals(createTaskForm.getKind(), ITDreamConstant.TASK_KIND_TASK)) {
      if (createTaskForm.getParentId() != null) {
        throw new BadRequestException("Task cannot have parent", ErrorCode.TASK_ERROR_NOT_PARENT);
      }
      // 1 simulation không thể tồn tại 2 task (kind = 1) có name giống nhau
      // nhưng có thể tồn tại 1 task (kind = 1) và 1 subtask (kind = 2) có name giống nhau
      Boolean existTask = taskRepository.existsByNameAndKindAndSimulationId(createTaskForm.getName(), ITDreamConstant.TASK_KIND_TASK, createTaskForm.getSimulationId());
      if (existTask) {
        throw new BadRequestException("Task name already exist", ErrorCode.TASK_ERROR_EXIST);
      }
    } else {
      if (createTaskForm.getParentId() == null) {
        throw new BadRequestException("Subtask must include parent", ErrorCode.TASK_ERROR_PARENT);
      }
      Task existTask = taskRepository.findByKindAndId(ITDreamConstant.TASK_KIND_TASK, createTaskForm.getParentId());
      if (existTask == null){
        throw new BadRequestException("Kind of parent cannot be a task", ErrorCode.TASK_ERROR_PARENT_NOT_KIND_TASK);
      }
      if (!Objects.equals(existTask.getName(), createTaskForm.getName())){
        throw new BadRequestException("Name subtask must be the same as name task", ErrorCode.TASK_ERROR_NAME);
      }
      // 1 simulation không thể tồn tại 2 subtask trong cùng 1 task có title giống nhau
      // Nhưng có thể tồn tại 2 subtask có title giống nhau nhưng phải khác task
      Boolean existSubtask = taskRepository.existsByTitleAndKindAndParentIdAndSimulationId(createTaskForm.getTitle(), ITDreamConstant.TASK_KIND_SUBTASK, createTaskForm.getParentId(), createTaskForm.getSimulationId());
      if (existSubtask) {
        throw new BadRequestException("Subtask title already exists", ErrorCode.TASK_ERROR_EXIST);
      }
    }

    Task task = taskMapper.fromCreateTaskFormToEntity(createTaskForm);
    if (Objects.equals(createTaskForm.getKind(), ITDreamConstant.TASK_KIND_SUBTASK)){
      Task parentTask = taskRepository.findByIdAndKind(createTaskForm.getParentId(), ITDreamConstant.TASK_KIND_TASK)
          .orElseThrow(() -> new NotFoundException("Task parent not found", ErrorCode.TASK_ERROR_PARENT_NOT_FOUND));
      task.setParent(parentTask);
    }
    if (StringUtils.isNotBlank(createTaskForm.getVideoPath())){
      task.setState(ITDreamConstant.STATE_TASK_PROCESSING);
    } else {
      task.setState(ITDreamConstant.STATE_TASK_DONE);
    }
    task.setSimulation(simulation);
    taskRepository.save(task);

    if (StringUtils.isNotBlank(createTaskForm.getVideoPath())){
      RequestProcessVideoMessageForm data = new RequestProcessVideoMessageForm();
      data.setId(task.getId());
      data.setKind(ITDreamConstant.KIND_TASK);
      data.setUrl(createTaskForm.getVideoPath());
      data.setTsSecond(tsSecond);
      processVideoService.sendProcessVideoMessage(data);
    }

    if (Objects.equals(ITDreamConstant.STATUS_ACTIVE, simulation.getStatus())){
      simulation.setStatus(ITDreamConstant.STATUS_WAITING_APPROVE);
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

  @GetMapping(value = "/student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_ST_L')")
  public ApiMessageDto<ResponseListDto<List<TaskDisplayDto>>> getListForStudent(@Valid TaskCriteria taskCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<TaskDisplayDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<TaskDisplayDto>> responseListDto = new ResponseListDto<>();
    taskCriteria.setStatus(ITDreamConstant.STATUS_ACTIVE);
    Page<Task> tasks = taskRepository.findAll(taskCriteria.getSpecification(), pageable);
    responseListDto.setContent(taskMapper.fromEntityToTaskDisplayDtoList(tasks.getContent()));
    responseListDto.setTotalElements(tasks.getTotalElements());
    responseListDto.setTotalPages(tasks.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/educator-list", produces = MediaType.APPLICATION_JSON_VALUE)
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

  @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_V')")
  public ApiMessageDto<TaskDto> get(@PathVariable("id") Long id){
    ApiMessageDto<TaskDto> apiMessageDto = new ApiMessageDto<>();
    Task task = taskRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    TaskDto taskDto = taskMapper.fromEntityToTaskDto(task);
    apiMessageDto.setData(taskDto);
    apiMessageDto.setMessage("Get success");
    return apiMessageDto;
  }

  @GetMapping(value = "/educator-get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_ED_V')")
  public ApiMessageDto<TaskEducatorDto> getForEducator(@PathVariable("id") Long id){
    ApiMessageDto<TaskEducatorDto> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Task task = taskRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    TaskEducatorDto taskDto = taskMapper.fromEntityToTaskEducatorDto(task);
    apiMessageDto.setData(taskDto);
    apiMessageDto.setMessage("Get success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('TA_ST_V')")
  public ApiMessageDto<TaskStudentDto> getForStudent(@PathVariable("id") Long id){
    ApiMessageDto<TaskStudentDto> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not an student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    Task task = taskRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    TaskStudentDto taskDto = taskMapper.fromEntityToTaskStudentDto(task);
    apiMessageDto.setData(taskDto);
    apiMessageDto.setMessage("Get success");
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

    Simulation simulation = task.getSimulation();
    if (simulation == null){
      throw new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND);
    }

    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Task in simulation cannot be updated", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }

    // Trường hợp nếu task tìm được có kind task (kind = 1)
    if (Objects.equals(task.getKind(), ITDreamConstant.TASK_KIND_TASK)){
      if (!Objects.equals(updateTaskForm.getName(), task.getName())){
        Boolean existTask = taskRepository.existsByNameAndKindAndSimulationId(updateTaskForm.getName(), ITDreamConstant.TASK_KIND_TASK, task.getSimulation().getId());
        if (existTask){
          throw new BadRequestException("Task name already exist", ErrorCode.TASK_ERROR_EXIST);
        }
      }

      if (updateTaskForm.getParentId() != null){
        throw new BadRequestException("Task cannot have parent", ErrorCode.TASK_ERROR_NOT_PARENT);
      }
    }
    // Trường hợp task tìm được có kind subtask (kind = 2)
    else {
      if (!Objects.equals(task.getTitle(), updateTaskForm.getTitle())){
        Boolean existSubtask = taskRepository.existsByTitleAndKindAndParentIdAndSimulationId(
            updateTaskForm.getTitle(), ITDreamConstant.TASK_KIND_SUBTASK, updateTaskForm.getParentId(), task.getSimulation().getId());
        if (existSubtask){
          throw new BadRequestException("Subtask title already exist", ErrorCode.TASK_ERROR_EXIST);
        }
      }

      if (updateTaskForm.getParentId() == null) {
        throw new BadRequestException("Subtask must include parent", ErrorCode.TASK_ERROR_PARENT);
      }

      Task parent = taskRepository.findById(updateTaskForm.getParentId()).orElseThrow(()
      -> new NotFoundException("Task parent not found", ErrorCode.TASK_ERROR_NOT_FOUND));
      if (!Objects.equals(parent.getKind(), ITDreamConstant.TASK_KIND_TASK)){
        throw new BadRequestException("Kind of parent cannot be a task", ErrorCode.TASK_ERROR_PARENT_NOT_KIND_TASK);
      }
      task.setParent(parent);
    }

    if (StringUtils.isNotBlank(updateTaskForm.getVideoPath())){
      if (StringUtils.isNotBlank(task.getVideoPath())){
        if (!Objects.equals(task.getVideoPath(), updateTaskForm.getVideoPath())){
          userBaseApiService.deleteByFilePath(task.getVideoPath());
          RequestProcessVideoMessageForm data = new RequestProcessVideoMessageForm();
          data.setId(updateTaskForm.getId());
          data.setUrl(updateTaskForm.getVideoPath());
          data.setKind(ITDreamConstant.KIND_TASK);
          data.setTsSecond(tsSecond);
          processVideoService.sendProcessVideoMessage(data);
        }
      } else {
        RequestProcessVideoMessageForm data = new RequestProcessVideoMessageForm();
        data.setId(updateTaskForm.getId());
        data.setUrl(updateTaskForm.getVideoPath());
        data.setKind(ITDreamConstant.KIND_TASK);
        data.setTsSecond(tsSecond);
        processVideoService.sendProcessVideoMessage(data);
      }
    }

    if (StringUtils.isNotBlank(updateTaskForm.getImagePath())){
      if (StringUtils.isNotBlank(task.getImagePath()) && !Objects.equals(task.getImagePath(), updateTaskForm.getImagePath())){
        userBaseApiService.deleteByFilePath(task.getImagePath());
      }
      task.setImagePath(updateTaskForm.getImagePath());
    }

    if (StringUtils.isNotBlank(updateTaskForm.getFilePath())) {
      if (StringUtils.isNotBlank(task.getFilePath()) && !Objects.equals(task.getFilePath(), updateTaskForm.getFilePath())){
        userBaseApiService.deleteByFilePath(task.getFilePath());
      }
      task.setFilePath(updateTaskForm.getFilePath());
    }

    taskMapper.fromUpdateTaskFormToEntity(updateTaskForm, task);
    taskRepository.save(task);

    if (Objects.equals(ITDreamConstant.STATUS_ACTIVE, task.getSimulation().getStatus())){
      simulation.setStatus(ITDreamConstant.STATUS_WAITING_APPROVE);
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
    Simulation simulation = task.getSimulation();
    if (simulation == null){
      throw new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND);
    }
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Task in simulation cannot be deleted", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    studentTaskQuestionProgressRepository.deleteAllByTaskAndSubtask(id);
    studentSubTaskProgressRepository.deleteAllByTaskAndSubtask(id);
    taskQuestionRepository.deleteAllByTaskAndSubtask(id);

    if (Objects.equals(task.getKind(), ITDreamConstant.TASK_KIND_SUBTASK)) {
      deleteTaskFiles(task);
      taskRepository.delete(task);
    } else if (Objects.equals(task.getKind(), ITDreamConstant.TASK_KIND_TASK)) {
      deleteTaskFiles(task);
      List<Task> subTasks = taskRepository.findAllByParentId(id);
      for (Task subTask : subTasks) {
        deleteTaskFiles(subTask);
      }
      taskRepository.deleteAllByParentId(id);
      taskRepository.delete(task);
    }

    if (Objects.equals(simulation.getStatus(), ITDreamConstant.STATUS_ACTIVE)){
      simulation.setStatus(ITDreamConstant.STATUS_WAITING_APPROVE);
      simulationRepository.save(simulation);
    }
    apiMessageDto.setMessage("Delete task success");
    return apiMessageDto;
  }

  private void deleteTaskFiles(Task task) {
    userBaseApiService.deleteByFilePath(task.getImagePath());
    userBaseApiService.deleteByFilePath(task.getFilePath());
    userBaseApiService.deleteByFilePath(task.getVideoPath());
  }
}
