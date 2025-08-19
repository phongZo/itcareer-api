package com.base.auth.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@ApiModel
public class GoogleLoginForm {
  @NotBlank(message = "accessToken cannot be null")
  @ApiModelProperty(name = "accessToken")
  private String accessToken;
  @NotEmpty(message = "email cannot be null")
  @ApiModelProperty(name = "email")
  private String email;
  @NotEmpty(message = "name cannot be null")
  @ApiModelProperty(name = "name")
  private String name;
  @ApiModelProperty(name = "picture")
  private String picture;
  @ApiModelProperty(name = "userRole")
  private String userRole;
}
