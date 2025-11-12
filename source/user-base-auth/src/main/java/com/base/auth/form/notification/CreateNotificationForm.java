package com.base.auth.form.notification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class CreateNotificationForm {
  @NotNull(message = "receiverId cannot be null")
  @ApiModelProperty(name = "receiverId")
  private Long receiverId;
  @NotEmpty(message = "title cannot be null")
  @ApiModelProperty(name = "title")
  private String title;
  @NotEmpty(message = "message cannot be null")
  @ApiModelProperty(name = "message")
  private String message;
  @NotEmpty(message = "refType cannot be null")
  @ApiModelProperty(name = "refType")
  private String refType;
  @NotNull(message = "refId cannot be null")
  @ApiModelProperty(name = "refId")
  private Long refId;
}
