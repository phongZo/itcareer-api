package com.base.auth.dto.subtask;

import com.base.auth.dto.task.TaskDisplayDto;
import lombok.Data;

@Data
public class SubTaskClientDto {
  private String title;
  private String introduction;
  private String content;
  private String imagePath;
  private String filePath;
  private String videoPath;
  private TaskDisplayDto task;
}
