package com.base.auth.service;

import com.base.auth.form.BaseMsgForm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQService {
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RabbitMQSender rabbitSender;

  @Value("${rabbitmq.backend.app}")
  String backendApp;

  public <T> void handleSendMsg(T data, String cmd, String queueName) {
    BaseMsgForm<T> form = new BaseMsgForm<>();
    form.setApp(backendApp);
    form.setCmd(cmd);
    form.setData(data);

    String msg;
    try {
      msg = objectMapper.writeValueAsString(form);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // create queue if existed
    rabbitSender.createQueueIfNotExist(queueName);

    log.error(msg);
    // push msg
    rabbitSender.send(msg, queueName);
  }
}
