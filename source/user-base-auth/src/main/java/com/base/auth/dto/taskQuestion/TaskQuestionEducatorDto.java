package com.base.auth.dto.taskQuestion;

import com.base.auth.dto.subtask.SubTaskClientDto;
import lombok.Data;

@Data
public class TaskQuestionEducatorDto {
  private Long id;
  private String question;
  private Integer questionType;
  private String options;
  private SubTaskClientDto subTask;
}
