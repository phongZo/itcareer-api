package com.base.auth.controller;

import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.notification.NotificationDto;
import com.base.auth.exception.NotFoundException;
import com.base.auth.mapper.NotificationMapper;
import com.base.auth.model.Notification;
import com.base.auth.repository.NotificationRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notification")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class NotificationController extends ABasicController{
  @Autowired
  NotificationRepository notificationRepository;

  @Autowired
  NotificationMapper notificationMapper;

  @GetMapping(value = "/student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('NO_STL')")
  public ApiMessageDto<List<NotificationDto>> getListForStudent(){
    ApiMessageDto<List<NotificationDto>> apiMessageDto = new ApiMessageDto<>();
    List<Notification> notifications = notificationRepository.findTop20ByReceiverIdOrderByCreatedDateDesc(getCurrentUser());
    List<NotificationDto> notificationDtos = notificationMapper.fromEntityToNotificationDisplayDtoList(notifications);
    apiMessageDto.setData(notificationDtos);
    apiMessageDto.setMessage("Get list notification success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('NO_STV')")
  public ApiMessageDto<NotificationDto> getForStudent(@PathVariable("id") Long id){
    ApiMessageDto<NotificationDto> apiMessageDto = new ApiMessageDto<>();
    Notification notification = notificationRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Notification not found", ErrorCode.NOTIFICATION_ERROR_NOT_FOUND));
    notification.setReadFlag(true);
    notificationRepository.save(notification);
    NotificationDto notificationDto = notificationMapper.fromEntityToNotificationDto(notification);
    apiMessageDto.setData(notificationDto);
    apiMessageDto.setMessage("Get notification success");
    return apiMessageDto;
  }
}
