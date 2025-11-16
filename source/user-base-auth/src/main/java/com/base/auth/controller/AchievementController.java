package com.base.auth.controller;

import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.achievement.AchievementDto;
import com.base.auth.dto.achievement.AchievementStudentDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.achievement.UpdateAchievementForm;
import com.base.auth.mapper.AchievementMapper;
import com.base.auth.model.Achievement;
import com.base.auth.model.criteria.AchievementCriteria;
import com.base.auth.repository.AchievementRepository;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/achievement")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AchievementController extends ABasicController{
  @Autowired
  AchievementRepository achievementRepository;

  @Autowired
  AchievementMapper achievementMapper;

  // Hàm update lại achievement sau khi upload file certificate
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<String> update(@Valid @RequestBody UpdateAchievementForm updateAchievementForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Achievement achievement = achievementRepository.findById(updateAchievementForm.getId()).orElseThrow(()
    -> new NotFoundException("Achievement not found", ErrorCode.ACHIEVEMENT_ERROR_NOT_FOUND));
    if (!Objects.equals(achievement.getStudent().getId(), getCurrentUser())){
      throw new BadRequestException("Student cannot allowed update", ErrorCode.ACHIEVEMENT_ERROR_NOT_AUTHORIZE);
    }
    achievement.setFilePath(updateAchievementForm.getFilePath());
    achievementRepository.save(achievement);
    apiMessageDto.setMessage("Update achievement success");
    return apiMessageDto;
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ACH_L')")
  public ApiMessageDto<ResponseListDto<List<AchievementDto>>> getList(AchievementCriteria achievementCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<AchievementDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<AchievementDto>> responseListDto = new ResponseListDto<>();
    Page<Achievement> achievements = achievementRepository.findAll(achievementCriteria.getSpecification(), pageable);
    responseListDto.setContent(achievementMapper.fromEntityToAchievementDtoList(achievements.getContent()));
    responseListDto.setTotalElements(achievements.getTotalElements());
    responseListDto.setTotalPages(achievements.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list achievement success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ACH_ST_L')")
  public ApiMessageDto<ResponseListDto<List<AchievementStudentDto>>> getListForStudent(AchievementCriteria achievementCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<AchievementStudentDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<AchievementStudentDto>> responseListDto = new ResponseListDto<>();
    achievementCriteria.setStudentId(getCurrentUser());
    Page<Achievement> achievements = achievementRepository.findAll(achievementCriteria.getSpecification(), pageable);
    responseListDto.setContent(achievementMapper.fromEntityToAchievementStudentDtoList(achievements.getContent()));
    responseListDto.setTotalElements(achievements.getTotalElements());
    responseListDto.setTotalPages(achievements.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list achievement success");
    return apiMessageDto;
  }
}
