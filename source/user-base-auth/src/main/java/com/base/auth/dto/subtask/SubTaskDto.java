package com.base.auth.dto.subtask;

import com.base.auth.dto.task.TaskDto;
import lombok.Data;

@Data
public class SubTaskDto {
  private Long id;
  private String title;
  private String introduction;
  private String content;
  private String imagePath;
  private String filePath;
  private String videoPath;
  private TaskDto task;
}
