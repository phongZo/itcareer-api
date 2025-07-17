package com.base.auth.dto.educator;

import com.base.auth.dto.account.AccountDto;
import java.util.Date;
import lombok.Data;

@Data
public class EducatorDto {
  private Long id;
  private Date birthday;
  private AccountDto account;
}
