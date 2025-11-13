package com.base.auth.form.notification;

import lombok.Data;

@Data
public class CreateNotificationForm {
  private Long id;
  private Long userId;
  private String title;
  private String message;
  private String refType;
  private Long refId;
}
