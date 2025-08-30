package com.base.auth.form.studentSubTaskProgress;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class RequestStudentSubTaskProgressForm {
  @NotNull(message = "task id cannot be null")
  @ApiModelProperty(name = "task id")
  private Long taskId;
  @ApiModelProperty(name = "file path")
  private String filePath;
}
