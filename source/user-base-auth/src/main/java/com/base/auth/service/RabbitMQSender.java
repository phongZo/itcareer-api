package com.base.auth.service;

import java.util.Properties;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQSender {
  @Autowired
  RabbitTemplate template;

  @Autowired
  RabbitAdmin rabbitAdmin;

  @Value("${rabbitmq.media.completed.process.video.queue}")
  String mediaCompletedProcessVideoQueue;

  public void send(String message, String queueName) {
    createQueueIfNotExist(queueName);
    if(message == null || StringUtils.isBlank(message)){
      log.info("-------> Can not send an empty or null message.");
      return;
    }
    template.convertAndSend(queueName, message);
    log.info(" [x] Sent '" + message + "', queueName: "+queueName);
  }

  public boolean isQueueExist(String queueName) {
    Properties properties = rabbitAdmin.getQueueProperties(queueName);

    return properties != null;
  }

  public void createQueueIfNotExist(String queueName) {
    if(!isQueueExist(queueName)){
      log.info("-------> Create queue name: " + queueName);
      rabbitAdmin.declareQueue(new Queue(queueName));
    }
  }

  public void removeQueue(String queueName){
    if(isQueueExist(queueName)){
      log.info("-------> Delete queue name: " + queueName);
      rabbitAdmin.deleteQueue(queueName);
    }
  }

  @PostConstruct
  public void initializeQueue(){
    createQueueIfNotExist(mediaCompletedProcessVideoQueue);
  }
}
