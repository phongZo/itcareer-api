package com.base.auth.dto.studentSubTaskProgress;

import com.base.auth.dto.subtask.SubTaskDisplayDto;
import lombok.Data;

@Data
public class StudentSubTaskProgressDisplayDto {
  private Long id;
  private SubTaskDisplayDto subTask;
}
