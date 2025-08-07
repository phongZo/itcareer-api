package com.base.auth.form.task;

import com.base.auth.validation.TaskKind;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class CreateTaskForm {
  @NotEmpty(message = "name cannot be null")
  @ApiModelProperty(name = "name")
  private String name;
  @NotEmpty(message = "description cannot be null")
  @ApiModelProperty(name = "description")
  private String description;
  @NotEmpty(message = "title cannot be null")
  @ApiModelProperty(name = "title")
  private String title;
  @ApiModelProperty(name = "introduction")
  private String introduction;
  @ApiModelProperty(name = "content")
  private String content;
  @ApiModelProperty(name = "imagePath")
  private String imagePath;
  @ApiModelProperty(name = "filePath")
  private String filePath;
  @ApiModelProperty(name = "videoPath")
  private String videoPath;
  @ApiModelProperty(name = "tsSecond")
  private Integer tsSecond;
  @TaskKind
  @ApiModelProperty(name = "kind")
  private Integer kind;
  @ApiModelProperty(name = "parentId")
  private Long parentId;
  @NotNull(message = "simulationId cannot be null")
  @ApiModelProperty(name = "simulationId")
  private Long simulationId;
}
