package com.base.auth.service;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.form.BaseMsgForm;
import com.base.auth.form.ProcessVideoSuccessForm;
import com.base.auth.form.RequestProcessVideoMessageForm;
import com.base.auth.model.Simulation;
import com.base.auth.model.Task;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.TaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessVideoService {
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RabbitMQService rabbitService;

  @Autowired
  SimulationRepository simulationRepository;

  @Autowired
  TaskRepository taskRepository;

  @Value("${rabbitmq.video.app}")
  private String videoApp;

  @Value("${rabbitmq.process.video.queue}")
  private String processVideoQueue;

  public void sendProcessVideoMessage(RequestProcessVideoMessageForm data) {
    rabbitService.handleSendMsg(data, UserBaseConstant.BACKEND_PROCESS_VIDEO_CMD, processVideoQueue);
  }

  @RabbitListener(queues = "${rabbitmq.media.completed.process.video.queue}")
  public void receiveMessage(String message){
    log.error("Received message: " + message);
    BaseMsgForm form;
    try {
      form = objectMapper.readValue(message,BaseMsgForm.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    if(form != null) {
      if(!form.getApp().equals(videoApp)) {
        log.error("===========> Invalid app: " + form.getApp());
        return;
      }
      if(form.getCmd().equals(UserBaseConstant.MEDIA_COMPLETED_PROCESS_VIDEO_CMD)){
        ProcessVideoSuccessForm data = objectMapper.convertValue(form.getData(), ProcessVideoSuccessForm.class);
        if (data.getTaskId() != null){
          // update task when received data success
          updateTaskProcessed(data);
        } else {
          updateSimulationProcessed(data);
        }
      }
      else {
        log.error("===========> Invalid cmd: " + form.getCmd());
      }
    }
  }

  void updateTaskProcessed(ProcessVideoSuccessForm processVideoSuccessForm){
    log.info("Update state task processed.............");
    Task task = taskRepository.findById(processVideoSuccessForm.getTaskId()).orElse(null);
    if (task != null){
      if (!processVideoSuccessForm.getIsSuccess()){
        task.setState(UserBaseConstant.STATE_TASK_FAIL);
      } else {
        task.setVideoPath(processVideoSuccessForm.getContentPath());
        task.setState(UserBaseConstant.STATE_TASK_DONE);
      }
      taskRepository.save(task);
    }
  }

  void updateSimulationProcessed(ProcessVideoSuccessForm processVideoSuccessForm){
    log.info("Update state simulation processed.............");
    Simulation simulation = simulationRepository.findById(processVideoSuccessForm.getSimulationId()).orElse(null);
    if (simulation != null){
      if (!processVideoSuccessForm.getIsSuccess()){
        simulation.setState(UserBaseConstant.STATE_SIMULATION_FAIL);
      } else {
        simulation.setVideoPath(processVideoSuccessForm.getContentPath());
        simulation.setState(UserBaseConstant.STATE_SIMULATION_DONE);
      }
      simulationRepository.save(simulation);
    }
  }
}
