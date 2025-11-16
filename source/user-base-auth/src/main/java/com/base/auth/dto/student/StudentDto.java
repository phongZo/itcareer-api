package com.base.auth.dto.student;

import com.base.auth.dto.account.AccountDto;
import lombok.Data;

@Data
public class StudentDto {
  private Long id;
  private AccountDto account;
}
