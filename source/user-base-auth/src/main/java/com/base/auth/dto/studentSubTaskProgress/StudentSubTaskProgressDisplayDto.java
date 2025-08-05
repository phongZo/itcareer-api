package com.base.auth.dto.studentSubTaskProgress;

import com.base.auth.dto.task.TaskDisplayDto;
import lombok.Data;

@Data
public class StudentSubTaskProgressDisplayDto {
  private Long id;
  private TaskDisplayDto task;
}
