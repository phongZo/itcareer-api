package com.base.auth.form;

import lombok.Data;

@Data
public class ProcessVideoSuccessForm {
  private Long simulationId;
  private Long taskId;
  private String thumbnail;
  private Boolean isSuccess;
  private String contentPath;
  private Long videoDuration;
}
