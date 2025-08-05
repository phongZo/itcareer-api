package com.base.auth.dto.task;

import lombok.Data;

@Data
public class TaskStudentDto {
  private Long id;
  private String name;
  private String description;
  private String title;
  private String introduction;
  private String content;
  private String imagePath;
  private String filePath;
  private String videoPath;
}
