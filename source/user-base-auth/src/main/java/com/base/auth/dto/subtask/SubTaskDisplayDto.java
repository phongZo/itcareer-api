package com.base.auth.dto.subtask;

import com.base.auth.dto.task.TaskDisplayDto;
import lombok.Data;

@Data
public class SubTaskDisplayDto {
  private Long id;
  private String title;
  private TaskDisplayDto task;
}
