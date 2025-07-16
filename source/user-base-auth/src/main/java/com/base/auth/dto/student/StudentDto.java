package com.base.auth.dto.student;

import com.base.auth.dto.account.AccountDto;
import java.util.Date;
import lombok.Data;

@Data
public class StudentDto {
  private Long id;
  private Date birthday;
  private AccountDto account;
}
