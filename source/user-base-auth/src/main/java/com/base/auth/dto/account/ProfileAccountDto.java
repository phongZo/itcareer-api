package com.base.auth.dto.account;

import lombok.Data;

@Data
public class ProfileAccountDto {
  private int kind;
  private String username;
  private String phone;
  private String email;
  private String fullName;
  private String avatar;
}
