package com.base.auth.service;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ErrorCode;
import com.base.auth.exception.BadRequestException;
import com.base.auth.form.notification.CreateNotificationForm;
import com.base.auth.mapper.NotificationMapper;
import com.base.auth.model.Notification;
import com.base.auth.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.transaction.Transactional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  RabbitMQService rabbitMQService;

  @Autowired
  ObjectMapper objectMapper;

  @Value("${rabbitmq.backend.app}")
  String msgApp;

  @Value("${rabbitmq.notification.queue}")
  private String notificationQueue;

  public void notifyStudent(@Valid CreateNotificationForm request){
    Boolean existNotification = notificationRepository.existsByReceiverIdAndRefId(
        request.getReceiverId(), request.getRefId());
    if (existNotification){
      throw new BadRequestException("Notification already exist", ErrorCode.NOTIFICATION_ERROR_EXIST);
    }
    Notification notification = notificationMapper.fromCreateNotificationFormToEntity(request);
    notificationRepository.save(notification);

    // Lấy thông tin notification để gửi sang websocket
    CreateNotificationForm newForm = new CreateNotificationForm();
    newForm.setId(notification.getId());
    newForm.setReceiverId(notification.getReceiverId());
    newForm.setTitle(notification.getTitle());
    newForm.setMessage(notification.getMessage());
    newForm.setRefType(notification.getRefType());
    newForm.setRefId(notification.getRefId());
    sendNotificationMessage(newForm);
  }

  // Gửi message notification vào queue
  private void sendNotificationMessage(CreateNotificationForm request){
    rabbitMQService.handleSendMsg(request, UserBaseConstant.BACKEND_POST_NOTIFICATION_CMD, notificationQueue);
  }
}
