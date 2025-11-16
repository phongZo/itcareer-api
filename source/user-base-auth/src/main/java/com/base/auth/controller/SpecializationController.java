package com.base.auth.controller;

import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.specialization.SpecializationAutoCompleteDto;
import com.base.auth.dto.specialization.SpecializationDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.specialization.CreateSpecializationForm;
import com.base.auth.form.specialization.UpdateSpecializationForm;
import com.base.auth.mapper.SpecializationMapper;
import com.base.auth.model.Specialization;
import com.base.auth.model.criteria.SpecializationCriteria;
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
@RequestMapping("/v1/specialization")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class SpecializationController extends ABasicController{
  @Autowired
  SpecializationRepository specializationRepository;

  @Autowired
  SpecializationMapper specializationMapper;

  @Autowired
  SimulationRepository simulationRepository;

  @PostMapping(value = "/create", produces= MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SP_C')")
  public ApiMessageDto<String> create(@Valid @RequestBody CreateSpecializationForm createSpecializationForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Boolean existSpecialization = specializationRepository.existsByName(createSpecializationForm.getName());
    if (existSpecialization){
      throw new BadRequestException("Specialization already exist", ErrorCode.SPECIALIZATION_ERROR_EXIST);
    }
    Specialization specialization = specializationMapper.fromCreateSpecializationFormToEntity(createSpecializationForm);
    specializationRepository.save(specialization);
    apiMessageDto.setMessage("Create specialization success");
    return apiMessageDto;
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SP_L')")
  public ApiMessageDto<ResponseListDto<List<SpecializationDto>>> getList(SpecializationCriteria specializationCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<SpecializationDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<SpecializationDto>> responseListDto = new ResponseListDto<>();
    Page<Specialization> specializations = specializationRepository.findAll(specializationCriteria.getSpecification(), pageable);
    responseListDto.setContent(specializationMapper.fromEntityToSpecializationDtoList(specializations.getContent()));
    responseListDto.setTotalElements(specializations.getTotalElements());
    responseListDto.setTotalPages(specializations.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping("/auto-complete")
  public ApiMessageDto<ResponseListDto<List<SpecializationAutoCompleteDto>>> listSpecializationAutoComplete(SpecializationCriteria specializationCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<SpecializationAutoCompleteDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<SpecializationAutoCompleteDto>> responseListDto = new ResponseListDto<>();
    Page<Specialization> specializations = specializationRepository.findAll(specializationCriteria.getSpecification(), pageable);
    responseListDto.setContent(specializationMapper.fromEntityToSpecializationAutoCompleteDtoList(specializations.getContent()));
    responseListDto.setTotalElements(specializations.getTotalElements());
    responseListDto.setTotalPages(specializations.getTotalPages());

    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SP_U')")
  public ApiMessageDto<String> update(@Valid @RequestBody UpdateSpecializationForm updateSpecializationForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Specialization specialization = specializationRepository.findById(updateSpecializationForm.getId()).orElseThrow(()
    -> new NotFoundException("Specialization not found"));
    if (!Objects.equals(updateSpecializationForm.getName(), specialization.getName())){
      Boolean existSpecialization = specializationRepository.existsByName(updateSpecializationForm.getName());
      if (existSpecialization){
        throw new BadRequestException("Specialization already exist", ErrorCode.SPECIALIZATION_ERROR_EXIST);
      }
    }
    specializationMapper.fromUpdateSpecializationFormToEntity(updateSpecializationForm, specialization);
    specializationRepository.save(specialization);
    apiMessageDto.setMessage("Update specialization success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('SP_D')")
  public ApiMessageDto<String> delete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Specialization specialization = specializationRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Specialization not found", ErrorCode.SPECIALIZATION_ERROR_NOT_FOUND));
    Boolean existSimulation = simulationRepository.existsBySpecializationId(id);
    if (existSimulation){
      throw new BadRequestException("Specialization cannot be deleted", ErrorCode.SPECIALIZATION_ERROR_DELETE);
    }
    specializationRepository.delete(specialization);
    apiMessageDto.setMessage("Delete specialization success");
    return apiMessageDto;
  }
}
