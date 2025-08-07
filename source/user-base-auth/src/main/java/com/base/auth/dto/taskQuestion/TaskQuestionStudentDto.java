package com.base.auth.dto.taskQuestion;

import com.base.auth.dto.task.TaskStudentDto;
import lombok.Data;

@Data
public class TaskQuestionStudentDto {
  private Long id;
  private String question;
  private String options;
  private TaskStudentDto task;
}
