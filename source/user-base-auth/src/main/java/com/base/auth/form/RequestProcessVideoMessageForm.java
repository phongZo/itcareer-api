package com.base.auth.form;
import lombok.Data;

@Data
public class RequestProcessVideoMessageForm {
  private Long simulationId;
  private Long taskId;
  private Integer kind;
  private String url;
  private Integer tsSecond;
}
