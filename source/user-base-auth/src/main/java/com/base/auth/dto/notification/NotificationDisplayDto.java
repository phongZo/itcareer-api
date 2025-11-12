package com.base.auth.dto.notification;

import lombok.Data;

@Data
public class NotificationDisplayDto {
  private Long id;
  private String title;
  private String message;
  private Boolean readFlag;
}
