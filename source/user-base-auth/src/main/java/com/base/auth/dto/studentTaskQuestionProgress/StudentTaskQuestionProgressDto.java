package com.base.auth.dto.studentTaskQuestionProgress;

import com.base.auth.dto.studentSubTaskProgress.StudentSubTaskProgressDto;
import com.base.auth.dto.taskQuestion.TaskQuestionDto;
import lombok.Data;

@Data
public class StudentTaskQuestionProgressDto {
  private Long id;
  private StudentSubTaskProgressDto studentSubTaskProgress;
  private TaskQuestionDto taskQuestion;
  private String answer;
  private Boolean isCorrect;
}
