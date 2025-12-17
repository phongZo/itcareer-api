package com.base.auth.dto.task;

import com.base.auth.model.Task;
import lombok.Data;

@Data
public class TaskEducatorDto {
  private Long id;
  private String name;
  private String description;
  private String title;
  private String introduction;
  private String content;
  private String imagePath;
  private String filePath;
  private String videoPath;
  private Integer kind;
  private Integer maxErrors;
  private Integer totalQuestion;
  private Task parent;
}
