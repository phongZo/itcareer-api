package com.base.auth.form.task;
import lombok.Data;

@Data
public class RequestProcessVideoMessageForm {
  private Long simulationId;
  private Long taskId;
  private String url;
  private Integer tsSecond;
}
