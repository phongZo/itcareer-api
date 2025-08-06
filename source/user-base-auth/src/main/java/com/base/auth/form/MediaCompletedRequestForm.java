package com.base.auth.form;

import com.base.auth.form.task.UpdateTaskVideoForm;
import lombok.Data;

@Data
public class MediaCompletedRequestForm {
  private String cmd;
  private UpdateTaskVideoForm data;
  private String app;
}
