package com.base.auth.form.user;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class VerifyUserForm {
  @NotEmpty(message = "OPT can not be null.")
  @ApiModelProperty(name = "otp", required = true)
  private String otp;

  @NotEmpty(message = "Email can not be null.")
  @ApiModelProperty(name = "idHash", required = true)
  private String idHash;
}
