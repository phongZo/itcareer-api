package com.base.auth.form.studentTaskQuestionProgress;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class CreateStudentTaskQuestionProgressForm {
  @NotNull(message = "studentSubTaskProgressId cannot be null")
  @ApiModelProperty(name = "studentSubTaskProgressId")
  private Long studentSubTaskProgressId;
  @NotNull(message = "taskQuestionId cannot be null")
  @ApiModelProperty(name = "taskQuestionId")
  private Long taskQuestionId;
  @NotEmpty(message = "answer cannot be null")
  @ApiModelProperty(name = "answer")
  private String answer;
  @ApiModelProperty(name = "isCorrect")
  private Boolean isCorrect;
}
