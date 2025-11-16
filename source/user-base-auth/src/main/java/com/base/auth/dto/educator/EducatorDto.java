package com.base.auth.dto.educator;

import com.base.auth.dto.account.AccountDto;
import lombok.Data;

@Data
public class EducatorDto {
  private Long id;
  private AccountDto account;
}
