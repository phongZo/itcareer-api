package com.base.auth.dto.educator;

import com.base.auth.dto.account.AccountAutoCompleteDto;
import lombok.Data;

@Data
public class EducatorAutoCompleteDto {
  private Long id;
  private AccountAutoCompleteDto accountAutoCompleteDto;
}
