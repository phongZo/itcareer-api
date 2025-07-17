package com.base.auth.dto.educator;

import com.base.auth.dto.account.ProfileAccountDto;
import java.util.Date;
import lombok.Data;

@Data
public class ProfileEducatorDto {
  private ProfileAccountDto profileAccountDto;
  private Date birthday;
}
