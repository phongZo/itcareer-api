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
  @NotEmpty(message = "description cannot be null")
  @ApiModelProperty(name = "description")
  private String description;
  @NotEmpty(message = "content cannot be null")
  @ApiModelProperty(name = "content")
  private String content;
  @NotNull(message = "simulationId cannot be null")
  @ApiModelProperty(name = "simulationId")
  private Long simulationId;
}
