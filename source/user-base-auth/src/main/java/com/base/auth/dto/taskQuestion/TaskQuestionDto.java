package com.base.auth.dto.taskQuestion;

import com.base.auth.dto.subtask.SubTaskDto;
import lombok.Data;

@Data
public class TaskQuestionDto {
  private Long id;
  private String question;
  private Integer questionType;
  private String options;
  private SubTaskDto subTask;
}
