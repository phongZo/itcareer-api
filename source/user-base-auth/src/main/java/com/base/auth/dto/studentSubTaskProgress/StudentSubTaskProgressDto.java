package com.base.auth.dto.studentSubTaskProgress;

import com.base.auth.dto.student.StudentDto;
import com.base.auth.dto.task.TaskDto;
import lombok.Data;

@Data
public class StudentSubTaskProgressDto {
  private Long id;
  private StudentDto student;
  private TaskDto task;
  private Integer currentAttempt;
  private Integer errorCount;
  private Integer state;
}
