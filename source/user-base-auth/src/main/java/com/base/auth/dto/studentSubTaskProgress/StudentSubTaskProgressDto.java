package com.base.auth.dto.studentSubTaskProgress;

import com.base.auth.dto.student.StudentDto;
import com.base.auth.dto.subtask.SubTaskDto;
import lombok.Data;

@Data
public class StudentSubTaskProgressDto {
  private Long id;
  private StudentDto student;
  private SubTaskDto subTask;
  private Integer currentAttempt;
  private Integer errorCount;
  private Integer state;
}
