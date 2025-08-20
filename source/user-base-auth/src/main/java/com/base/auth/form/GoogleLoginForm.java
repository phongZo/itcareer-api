package com.base.auth.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
@ApiModel
public class GoogleLoginForm {
  @NotBlank(message = "accessToken cannot be null")
  @ApiModelProperty(name = "accessToken")
  private String accessToken;
  @ApiModelProperty(name = "userRole")
  private String userRole;
}
