package com.base.auth.form.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class RequestProcessVideoMessageForm {
  @NotNull(message = "simulationId cannot be null")
  @ApiModelProperty(name = "simulationId")
  private Long simulationId;
  @NotNull(message = "taskId cannot be null")
  @ApiModelProperty(name = "taskId")
  private Long taskId;
  @NotEmpty(message = "url cannot be null")
  @ApiModelProperty(name = "url")
  private String url;
  @NotNull(message = "tsSecond cannot be null")
  @ApiModelProperty(name = "tsSecond")
  private Integer tsSecond;
}
