package com.base.auth.service;

import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.notification.NotificationDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.form.notification.CreateNotificationForm;
import com.base.auth.mapper.NotificationMapper;
import com.base.auth.model.Notification;
import com.base.auth.repository.NotificationRepository;
import javax.transaction.Transactional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional
public class NotificationService {
  @Autowired
  NotificationRepository notificationRepository;

  @Autowired
  NotificationMapper notificationMapper;

  @Autowired
  SimpMessagingTemplate messagingTemplate;

  public void notifyStudent(@Valid CreateNotificationForm request){
    Boolean existNotification = notificationRepository.existsByReceiverIdAndRefId(
        request.getReceiverId(), request.getRefId());
    if (existNotification){
      throw new BadRequestException("Notification already exist", ErrorCode.NOTIFICATION_ERROR_EXIST);
    }
    Notification notification = notificationMapper.fromCreateNotificationFormToEntity(request);
    notificationRepository.save(notification);
    NotificationDto notificationDto = notificationMapper.fromEntityToNotificationDto(notification);
    try{
      String destination = "/topic/notification/" + notification.getReceiverId();
      messagingTemplate.convertAndSend(destination, notificationDto);
      log.info("=====> MESSAGE SEND SUCCESS" );
    }catch (Exception ex){
      log.warn("Failed to send realtime notification to receiverId={}, refId={}. Error: {}",
          notification.getReceiverId(), notification.getRefId(), ex.getMessage(), ex);
    }
  }
}
