package com.base.auth.form.subtask;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSubTaskForm {
  @NotNull(message = "id cannot be null")
  @ApiModelProperty(name = "id")
  private Long id;
  @NotEmpty(message = "title cannot be null")
  @ApiModelProperty(name = "title")
  private String title;
  @NotEmpty(message = "introduction cannot be null")
  @ApiModelProperty(name = "introduction")
  private String introduction;
  @NotEmpty(message = "content cannot be null")
  @ApiModelProperty(name = "content")
  private String content;
  @ApiModelProperty(name = "imagePath")
  private String imagePath;
  @ApiModelProperty(name = "filePath")
  private String filePath;
  @ApiModelProperty(name = "videoPath")
  private String videoPath;
  @NotNull(message = "taskId cannot be null")
  @ApiModelProperty(name = "taskId")
  private Long taskId;
}
