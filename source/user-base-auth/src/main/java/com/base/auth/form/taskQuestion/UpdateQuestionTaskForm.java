package com.base.auth.form.taskQuestion;

import com.base.auth.validation.QuestionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class UpdateQuestionTaskForm {
  @NotNull(message = "id cannot be null")
  @ApiModelProperty(name = "id")
  private Long id;
  @NotEmpty(message = "question cannot be null")
  @ApiModelProperty(name = "question")
  private String question;
  @NotNull(message = "question type cannot be null")
  @QuestionType
  private Integer questionType;
  @ApiModelProperty(name = "options")
  private String options;
  @NotNull(message = "subtaskId cannot be null")
  @ApiModelProperty(name = "subtaskId")
  private Long subtaskId;
}
