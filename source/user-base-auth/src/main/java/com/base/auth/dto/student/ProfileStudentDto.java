package com.base.auth.dto.student;

import com.base.auth.dto.account.ProfileAccountDto;
import java.util.Date;
import lombok.Data;

@Data
public class ProfileStudentDto {
  private ProfileAccountDto profileAccountDto;
  private Date birthday;
}
