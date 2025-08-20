package com.base.auth.form;

import lombok.Data;

@Data
public class GoogleUserInfoForm {
  private String email;
  private String userName;
  private String fullName;
  private String avatarPath;
}
