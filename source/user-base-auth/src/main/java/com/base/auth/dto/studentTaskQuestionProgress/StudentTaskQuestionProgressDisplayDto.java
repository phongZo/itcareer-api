package com.base.auth.dto.studentTaskQuestionProgress;

import com.base.auth.dto.studentSubTaskProgress.StudentSubTaskProgressDisplayDto;
import com.base.auth.dto.taskQuestion.TaskQuestionStudentDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
public class StudentTaskQuestionProgressDisplayDto {
  private Long id;
  private StudentSubTaskProgressDisplayDto studentSubTaskProgress;
  @JsonIgnoreProperties({"task"})
  private TaskQuestionStudentDto taskQuestion;
  private String answer;
  private Boolean isCorrect;
}
