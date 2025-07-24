package com.base.auth.form.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class UpdateTaskForm {
  @NotNull(message = "id cannot be null")
  @ApiModelProperty(name = "id")
  private Long id;
  @NotEmpty(message = "name cannot be null")
  @ApiModelProperty(name = "name")
  private String name;
  @NotNull(message = "simulationId cannot be null")
  @ApiModelProperty(name = "simulationId")
  private Long simulationId;
}
