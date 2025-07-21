package com.base.auth.controller;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.simulation.SimulationAutoCompleteDto;
import com.base.auth.dto.simulation.SimulationClientDto;
import com.base.auth.dto.simulation.SimulationDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.simulation.CreateSimulationForm;
import com.base.auth.form.simulation.RequestSimulationIdForm;
import com.base.auth.form.simulation.UpdateSimulationForm;
import com.base.auth.mapper.SimulationMapper;
import com.base.auth.model.Educator;
import com.base.auth.model.Simulation;
import com.base.auth.model.Specialization;
import com.base.auth.model.criteria.SimulationCriteria;
import com.base.auth.repository.EducatorRepository;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.SpecializationRepository;
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

  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_C')")
  public ApiMessageDto<String> create(@Valid @RequestBody CreateSimulationForm createSimulationForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Educator educator = educatorRepository.findById(getCurrentUser()).orElseThrow(()
    -> new NotFoundException("Educator not found", ErrorCode.USER_ERROR_NOT_FOUND));
    if (!Objects.equals(educator.getAccount().getKind(), UserBaseConstant.USER_KIND_EDUCATOR)){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Specialization specialization = specializationRepository.findById(createSimulationForm.getSpecializationId()).orElseThrow(()
    -> new NotFoundException("Specialization not found", ErrorCode.SPECIALIZATION_ERROR_NOT_FOUND));
    Simulation simulation = simulationRepository.findByTitle(createSimulationForm.getTitle()).orElse(null);
    if (simulation != null && !Objects.equals(simulation.getEducator().getId(), educator.getId())){
      throw new BadRequestException("Simulation already exist", ErrorCode.SIMULATION_ERROR_EXIST);
    }
    simulation = simulationMapper.fromCreateSimulationFormToEntity(createSimulationForm);
    simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
    simulation.setSpecialization(specialization);
    simulation.setEducator(educator);
    simulationRepository.save(simulation);
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
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_V')")
  public ApiMessageDto<SimulationDto> get(@PathVariable("id") Long id){
    ApiMessageDto<SimulationDto> apiMessageDto = new ApiMessageDto<>();
    Simulation simulation = simulationRepository.findById(id).orElseThrow(() ->
        new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!specializationRepository.existsById(simulation.getSpecialization().getId())){
      throw new NotFoundException("Specialization not found", ErrorCode.SPECIALIZATION_ERROR_NOT_FOUND);
    }
    if (!educatorRepository.existsById(simulation.getEducator().getId())){
      throw new NotFoundException("Educator not found", ErrorCode.USER_ERROR_NOT_FOUND);
    }
    SimulationDto simulationDto = simulationMapper.fromEntityToSimulationDto(simulation);
    apiMessageDto.setData(simulationDto);
    apiMessageDto.setMessage("Get success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_ST_L')")
  public ApiMessageDto<ResponseListDto<List<SimulationAutoCompleteDto>>> getListForStudent(Pageable pageable){
    ApiMessageDto<ResponseListDto<List<SimulationAutoCompleteDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<SimulationAutoCompleteDto>> responseListDto = new ResponseListDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    Page<Simulation> simulations = simulationRepository.findAllByStatus(UserBaseConstant.STATUS_ACTIVE, pageable);
    responseListDto.setContent(simulationMapper.fromEntityToSimulationAutoCompleteDtoList(simulations.getContent()));
    responseListDto.setTotalElements(simulations.getTotalElements());
    responseListDto.setTotalPages(simulations.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/educator-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_ED_L')")
  public ApiMessageDto<ResponseListDto<List<SimulationAutoCompleteDto>>> getListForEducator(
      SimulationCriteria simulationCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<SimulationAutoCompleteDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<SimulationAutoCompleteDto>> responseListDto = new ResponseListDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    simulationCriteria.setEducatorId(getCurrentUser());
    Page<Simulation> simulations = simulationRepository.findAll(simulationCriteria.getSpecification(), pageable);
    responseListDto.setContent(simulationMapper.fromEntityToSimulationAutoCompleteDtoList(simulations.getContent()));
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
    if (!specializationRepository.existsById(simulation.getSpecialization().getId())){
      throw new NotFoundException("Specialization not found", ErrorCode.SPECIALIZATION_ERROR_NOT_FOUND);
    }
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
    if (!specializationRepository.existsById(simulation.getSpecialization().getId())){
      throw new NotFoundException("Specialization not found", ErrorCode.SPECIALIZATION_ERROR_NOT_FOUND);
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
    Educator educator = educatorRepository.findById(getCurrentUser()).orElseThrow(()
    -> new NotFoundException("Educator not found"));
    if (!Objects.equals(educator.getAccount().getKind(), UserBaseConstant.USER_KIND_EDUCATOR)){
      throw new BadRequestException("User is not an educator",ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Simulation simulation = simulationRepository.findById(updateSimulationForm.getId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), educator.getId())){
      throw new BadRequestException("Simulation cannot be updated", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    if (updateSimulationForm.getSpecializationId() != null && !Objects.equals(simulation.getSpecialization().getId(), updateSimulationForm.getSpecializationId())){
      Specialization specialization = specializationRepository.findById(updateSimulationForm.getSpecializationId()).orElseThrow(()
          -> new NotFoundException("Specialization not found", ErrorCode.SPECIALIZATION_ERROR_NOT_FOUND));
      simulation.setSpecialization(specialization);
    }
    if (!Objects.equals(UserBaseConstant.STATUS_ACTIVE, simulation.getStatus())){
      throw new BadRequestException("Simulation cannot active", ErrorCode.SIMULATION_ERROR_NOT_ACTIVE);
    }
    simulationMapper.fromUpdateSimulationFormToEntity(updateSimulationForm, simulation);
    simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
    simulationRepository.save(simulation);
    apiMessageDto.setMessage("Update success. Please wait for approval");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/approve-delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_APD')")
  public ApiMessageDto<String> approveDelete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Simulation simulation = simulationRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(UserBaseConstant.STATUS_WAITING_APPROVE, simulation.getStatus())){
      throw new BadRequestException("Simulation can not be deleted", ErrorCode.SIMULATION_ERROR_NOT_DELETE);
    }
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
    if (!Objects.equals(UserBaseConstant.STATUS_WAITING_APPROVE, simulation.getStatus())){
      throw new BadRequestException("Simulation can not be deleted", ErrorCode.SIMULATION_ERROR_NOT_DELETE);
    }
    simulation.setStatus(UserBaseConstant.STATUS_ACTIVE);
    simulationRepository.save(simulation);
    apiMessageDto.setMessage("Reject delete simulation success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/educator-request-delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SI_E_RED')")
  public ApiMessageDto<String> requestDelete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Simulation simulation = simulationRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(UserBaseConstant.STATUS_ACTIVE, simulation.getStatus())){
      throw new BadRequestException("Request for deletion is currently being approved", ErrorCode.SIMULATION_ERROR_APPROVE);
    }
    simulation.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
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
    if (!Objects.equals(UserBaseConstant.STATUS_WAITING_APPROVE, simulation.getStatus())){
      throw new BadRequestException("Simulation cannot approve", ErrorCode.SIMULATION_ERROR_APPROVE);
    }
    simulation.setStatus(UserBaseConstant.STATUS_ACTIVE);
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
    if (!Objects.equals(UserBaseConstant.STATUS_WAITING_APPROVE, simulation.getStatus())){
      throw new BadRequestException("Simulation cannot approve", ErrorCode.SIMULATION_ERROR_APPROVE);
    }
    simulation.setStatus(UserBaseConstant.STATUS_REJECT);
    simulationRepository.save(simulation);
    apiMessageDto.setMessage("Reject simulation success");
    return apiMessageDto;
  }
}
