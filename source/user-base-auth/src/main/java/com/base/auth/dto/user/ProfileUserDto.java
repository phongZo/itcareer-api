package com.base.auth.dto.user;

import com.base.auth.dto.account.ProfileAccountDto;
import java.util.Date;
import lombok.Data;

@Data
public class ProfileUserDto {
  private ProfileAccountDto profileAccountDto;
  private Date birthday;
}
