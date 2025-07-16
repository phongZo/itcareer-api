package com.base.auth.dto.student;

import com.base.auth.dto.account.AccountAutoCompleteDto;
import lombok.Data;

@Data
public class StudentAutoCompleteDto {
  private Long id;
  private AccountAutoCompleteDto accountAutoCompleteDto;
}
