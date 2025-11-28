package com.base.auth.controller;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.simulation.SimulationDisplayDto;
import com.base.auth.dto.simulation.SimulationClientDto;
import com.base.auth.dto.simulation.SimulationDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.simulation.CreateSimulationForm;
import com.base.auth.form.simulation.RequestSimulationIdForm;
import com.base.auth.form.simulation.UpdateSimulationForm;
import com.base.auth.form.RequestProcessVideoMessageForm;
import com.base.auth.mapper.SimulationMapper;
import com.base.auth.model.Educator;
import com.base.auth.model.Simulation;
import com.base.auth.model.Specialization;
import com.base.auth.model.Task;
import com.base.auth.model.criteria.SimulationCriteria;
import com.base.auth.repository.AchievementRepository;
import com.base.auth.repository.EducatorRepository;
import com.base.auth.repository.ReviewRepository;
import com.base.auth.repository.ReviewSubmissionRepository;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.SpecializationRepository;
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
@RequestMapping("/v1/simulation")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class SimulationController extends ABasicController{
  @Autowired
  SimulationRepository simulationRepository;

  @Autowired
  SimulationMapper simulationMapper;

  @Autowired
  SpecializationRepository specializationRepository;

  @Autowired
  EducatorRepository educatorRepository;

  @Autowired
  TaskRepository taskRepository;

  @Autowired
  TaskQuestionRepository taskQuestionRepository;

  @Autowired
  StudentSubTaskProgressRepository studentSubTaskProgressRepository;

  @Autowired
  StudentTaskQuestionProgressRepository studentTaskQuestionProgressRepository;

  @Autowired
  ProcessVideoService processVideoService;

  @Autowired
  ReviewRepository reviewRepository;

  @Autowired
  AchievementRepository achievementRepository;

  @Autowired
  ReviewSubmissionRepository reviewSubmissionRepository;

  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_C')")
  public ApiMessageDto<String> create(@Valid @RequestBody CreateSimulationForm createSimulationForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Educator educator = educatorRepository.findById(getCurrentUser()).orElseThrow(()
        -> new NotFoundException("Educator not found"));
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Specialization specialization = specializationRepository.findById(createSimulationForm.getSpecializationId()).orElseThrow(()
    -> new NotFoundException("Specialization not found", ErrorCode.SPECIALIZATION_ERROR_NOT_FOUND));
    boolean existSimulation = simulationRepository.existsByTitleAndEducatorId(createSimulationForm.getTitle(), getCurrentUser());
    if (existSimulation){
      throw new BadRequestException("Simulation already exist", ErrorCode.SIMULATION_ERROR_EXIST);
    }
    Simulation simulation = simulationMapper.fromCreateSimulationFormToEntity(createSimulationForm);
    simulation.setStatus(ITDreamConstant.STATUS_WAITING_APPROVE);
    simulation.setSpecialization(specialization);
    simulation.setEducator(educator);
    if (createSimulationForm.getVideoPath() != null){
      simulation.setState(ITDreamConstant.STATE_SIMULATION_PROCESSING);
    } else {
      simulation.setState(ITDreamConstant.STATE_SIMULATION_DONE);
    }
    simulationRepository.save(simulation);
    if (createSimulationForm.getVideoPath() != null){
      RequestProcessVideoMessageForm data = new RequestProcessVideoMessageForm();
      data.setId(simulation.getId());
      data.setKind(ITDreamConstant.KIND_SIMULATION);
      data.setUrl(createSimulationForm.getVideoPath());
      data.setTsSecond(tsSecond);
      processVideoService.sendProcessVideoMessage(data);
    }
    apiMessageDto.setMessage("Create simulation success. Please wait for approval");
    return apiMessageDto;
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_L')")
  public ApiMessageDto<ResponseListDto<List<SimulationDto>>> getList(SimulationCriteria simulationCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<SimulationDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<SimulationDto>> responseListDto = new ResponseListDto<>();
    Page<Simulation> simulations = simulationRepository.findAll(simulationCriteria.getSpecification(), pageable);
    responseListDto.setContent(simulationMapper.fromEntityToSimulationDtoList(simulations.getContent()));
    responseListDto.setTotalElements(simulations.getTotalElements());
    responseListDto.setTotalPages(simulations.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list simulation success");
    return apiMessageDto;
  }

  @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_V')")
  public ApiMessageDto<SimulationDto> get(@PathVariable("id") Long id){
    ApiMessageDto<SimulationDto> apiMessageDto = new ApiMessageDto<>();
    Simulation simulation = simulationRepository.findById(id).orElseThrow(() ->
        new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    SimulationDto simulationDto = simulationMapper.fromEntityToSimulationDto(simulation);
    apiMessageDto.setData(simulationDto);
    apiMessageDto.setMessage("Get success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_ST_L')")
  public ApiMessageDto<ResponseListDto<List<SimulationDisplayDto>>> getListForStudent(Pageable pageable){
    ApiMessageDto<ResponseListDto<List<SimulationDisplayDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<SimulationDisplayDto>> responseListDto = new ResponseListDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    Page<Simulation> simulations = simulationRepository.findAllByStatus(ITDreamConstant.STATUS_ACTIVE, pageable);
    List<SimulationDisplayDto> simulationDtos =simulationMapper.fromEntityToSimulationDisplayDtoList(simulations.getContent());
    for (SimulationDisplayDto simulationDto : simulationDtos){
      Long countTask = taskRepository.countByKindAndSimulationId(ITDreamConstant.TASK_KIND_SUBTASK,simulationDto.getId());
      Long countProgress = studentSubTaskProgressRepository.countByStateAndStudentIdAndTaskSimulationId(
          ITDreamConstant.STATE_STUDENT_SUBTASK_PROGRESS_COMPLETED, getCurrentUser(),
          simulationDto.getId());
      if (countTask > 0){
        Float progress = ((countProgress * 1F) / countTask) * 100;
        simulationDto.setPercent(progress);
      }
    }
    responseListDto.setContent(simulationDtos);
    responseListDto.setTotalElements(simulations.getTotalElements());
    responseListDto.setTotalPages(simulations.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/educator-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_ED_L')")
  public ApiMessageDto<ResponseListDto<List<SimulationDisplayDto>>> getListForEducator(
      SimulationCriteria simulationCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<SimulationDisplayDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<SimulationDisplayDto>> responseListDto = new ResponseListDto<>();
    simulationCriteria.setEducatorId(getCurrentUser());
    Page<Simulation> simulations = simulationRepository.findAll(simulationCriteria.getSpecification(), pageable);
    responseListDto.setContent(simulationMapper.fromEntityToSimulationDisplayDtoList(simulations.getContent()));
    responseListDto.setTotalElements(simulations.getTotalElements());
    responseListDto.setTotalPages(simulations.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_ST_V')")
  public ApiMessageDto<SimulationClientDto> getSimulationForStudent(@PathVariable("id") Long id){
    ApiMessageDto<SimulationClientDto> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    Simulation simulation = simulationRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    SimulationClientDto simulationDto = simulationMapper.fromEntityToSimulationClientDto(simulation);
    apiMessageDto.setData(simulationDto);
    apiMessageDto.setMessage("Get success");
    return apiMessageDto;
  }

  @GetMapping(value = "/educator-get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_ED_V')")
  public ApiMessageDto<SimulationClientDto> getSimulationForEducator(@PathVariable("id") Long id){
    ApiMessageDto<SimulationClientDto> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Simulation simulation = simulationRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be read", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    SimulationClientDto simulationDto = simulationMapper.fromEntityToSimulationClientDto(simulation);
    apiMessageDto.setData(simulationDto);
    apiMessageDto.setMessage("Get success");
    return apiMessageDto;
  }

  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_U')")
  public ApiMessageDto<String> update(@Valid @RequestBody UpdateSimulationForm updateSimulationForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator");
    }
    Simulation simulation = simulationRepository.findById(updateSimulationForm.getId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Simulation cannot be updated", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    if (updateSimulationForm.getSpecializationId() != null && !Objects.equals(simulation.getSpecialization().getId(), updateSimulationForm.getSpecializationId())){
      Specialization specialization = specializationRepository.findById(updateSimulationForm.getSpecializationId()).orElseThrow(()
          -> new NotFoundException("Specialization not found", ErrorCode.SPECIALIZATION_ERROR_NOT_FOUND));
      simulation.setSpecialization(specialization);
    }

    if (StringUtils.isNotBlank(updateSimulationForm.getVideoPath())){
      if (StringUtils.isNotBlank(simulation.getVideoPath())){
        if (!Objects.equals(simulation.getVideoPath(), updateSimulationForm.getVideoPath())){
          userBaseApiService.deleteByFilePath(simulation.getVideoPath());
          RequestProcessVideoMessageForm data = new RequestProcessVideoMessageForm();
          data.setId(simulation.getId());
          data.setKind(ITDreamConstant.KIND_SIMULATION);
          data.setUrl(updateSimulationForm.getVideoPath());
          data.setTsSecond(tsSecond);
          processVideoService.sendProcessVideoMessage(data);
        }
      } else {
        RequestProcessVideoMessageForm data = new RequestProcessVideoMessageForm();
        data.setId(simulation.getId());
        data.setKind(ITDreamConstant.KIND_SIMULATION);
        data.setUrl(updateSimulationForm.getVideoPath());
        data.setTsSecond(tsSecond);
        processVideoService.sendProcessVideoMessage(data);
      }
    }

    if (StringUtils.isNotBlank(updateSimulationForm.getImagePath())){
      if (StringUtils.isNotBlank(simulation.getImagePath()) && !Objects.equals(simulation.getImagePath(), updateSimulationForm.getImagePath())){
        userBaseApiService.deleteByFilePath(simulation.getImagePath());
      }
      simulation.setImagePath(updateSimulationForm.getImagePath());
    }

    simulationMapper.fromUpdateSimulationFormToEntity(updateSimulationForm, simulation);
    simulation.setStatus(ITDreamConstant.STATUS_WAITING_APPROVE);
    simulationRepository.save(simulation);
    apiMessageDto.setMessage("Update success. Please wait for approval");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/approve-delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_APD')")
  @Transactional
  public ApiMessageDto<String> approveDelete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Simulation simulation = simulationRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(ITDreamConstant.STATUS_WAITING_APPROVE_DELETE, simulation.getStatus())){
      throw new BadRequestException("Simulation cannot be deleted", ErrorCode.SIMULATION_ERROR_NOT_DELETE);
    }
    List<Task> tasks = taskRepository.findAllBySimulationId(id);
    for (Task task : tasks){
      deleteTaskFiles(task);
    }
    userBaseApiService.deleteByFilePath(simulation.getImagePath());
    userBaseApiService.deleteByFilePath(simulation.getVideoPath());
    studentTaskQuestionProgressRepository.deleteAllBySimulationId(id);
    studentSubTaskProgressRepository.deleteAllBySimulationId(id);
    taskQuestionRepository.deleteAllBySimulationId(id);
    taskRepository.deleteAllSubTaskBySimulationId(id);
    taskRepository.deleteAllTaskBySimulationId(id);
    reviewRepository.deleteBySimulationId(id);
    reviewSubmissionRepository.deleteBySimulationId(id);
    achievementRepository.setNullSimulationId(id);
    simulationRepository.delete(simulation);
    apiMessageDto.setMessage("Approve delete simulation success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/reject-delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_RJD')")
  public ApiMessageDto<String> rejectDelete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Simulation simulation = simulationRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(ITDreamConstant.STATUS_WAITING_APPROVE_DELETE, simulation.getStatus())){
      throw new BadRequestException("Simulation can not be deleted", ErrorCode.SIMULATION_ERROR_NOT_DELETE);
    }
    simulation.setStatus(ITDreamConstant.STATUS_ACTIVE);
    simulationRepository.save(simulation);
    apiMessageDto.setMessage("Reject delete simulation success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/educator-request-delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_ED_RED')")
  public ApiMessageDto<String> requestDelete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Simulation simulation = simulationRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(ITDreamConstant.STATUS_ACTIVE, simulation.getStatus())){
      throw new BadRequestException("Request for deletion is currently being approved", ErrorCode.SIMULATION_ERROR_APPROVE);
    }
    simulation.setStatus(ITDreamConstant.STATUS_WAITING_APPROVE_DELETE);
    simulationRepository.save(simulation);
    apiMessageDto.setMessage("Request delete simulation success");
    return apiMessageDto;
  }

  @PutMapping(value = "/approve", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_AP')")
  public ApiMessageDto<String> approve(@Valid @RequestBody RequestSimulationIdForm requestSimulationIdForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Simulation simulation = simulationRepository.findById(requestSimulationIdForm.getId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(ITDreamConstant.STATUS_WAITING_APPROVE, simulation.getStatus())){
      throw new BadRequestException("Simulation cannot approve", ErrorCode.SIMULATION_ERROR_APPROVE);
    }
    simulation.setStatus(ITDreamConstant.STATUS_ACTIVE);
    simulationRepository.save(simulation);
    apiMessageDto.setMessage("Approve simulation success");
    return apiMessageDto;
  }

  @PutMapping(value = "/reject", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_RJ')")
  public ApiMessageDto<String> reject(@Valid @RequestBody RequestSimulationIdForm requestSimulationIdForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Simulation simulation = simulationRepository.findById(requestSimulationIdForm.getId()).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(ITDreamConstant.STATUS_WAITING_APPROVE, simulation.getStatus())){
      throw new BadRequestException("Simulation cannot approve", ErrorCode.SIMULATION_ERROR_APPROVE);
    }
    simulation.setStatus(ITDreamConstant.STATUS_REJECT);
    simulationRepository.save(simulation);
    apiMessageDto.setMessage("Reject simulation success");
    return apiMessageDto;
  }

  private void deleteTaskFiles(Task task) {
    userBaseApiService.deleteByFilePath(task.getImagePath());
    userBaseApiService.deleteByFilePath(task.getFilePath());
    userBaseApiService.deleteByFilePath(task.getVideoPath());
  }
}
