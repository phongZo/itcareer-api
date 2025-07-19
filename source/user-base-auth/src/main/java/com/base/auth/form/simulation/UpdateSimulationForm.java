package com.base.auth.form.simulation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class UpdateSimulationForm {
  @NotNull(message = "id cannot be null")
  @ApiModelProperty(name = "id")
  private Long id;
  @NotEmpty(message = "title cannot be null")
  @ApiModelProperty(name = "title")
  private String title;
  @NotEmpty(message = "overview cannot be null")
  @ApiModelProperty(name = "overview")
  private String overview;
  @NotEmpty(message = "description cannot be null")
  @ApiModelProperty(name = "description")
  private String description;
  @ApiModelProperty(name = "level")
  private Integer level;
  @NotEmpty(message = "totalEstimatedTime cannot be null")
  @ApiModelProperty(name = "totalEstimatedTime")
  private String totalEstimatedTime;
  private String imagePath;
  private String videoPath;
  @NotNull(message = "specializationId cannot be null")
  @ApiModelProperty(name = "specializationId")
  private Long specializationId;
}
