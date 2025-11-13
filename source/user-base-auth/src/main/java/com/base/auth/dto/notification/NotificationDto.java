package com.base.auth.dto.notification;

import java.util.Date;
import lombok.Data;

@Data
public class NotificationDto {
  private Long id;
  private String title;
  private String message;
  private String refType;
  private Long refId;
  private Boolean readFlag;
  private Date createdDate;
}
