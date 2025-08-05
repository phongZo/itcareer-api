package com.base.auth.config;

import com.base.auth.constant.UserBaseConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
  @Bean
  public Queue videoResultQueue(){
    return new Queue(UserBaseConstant.MEDIA_COMPLETED_PROCESS_VIDEO, true);
  }
}
