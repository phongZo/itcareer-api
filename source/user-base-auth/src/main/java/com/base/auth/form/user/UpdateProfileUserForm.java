package com.base.auth.form.user;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Data;

@Data
public class UpdateProfileUserForm {
  @ApiModelProperty(name = "username")
  private String username;
  @ApiModelProperty(name = "fullname")
  private String fullname;
  @ApiModelProperty(name = "birthday")
  private Date birthday;
  @ApiModelProperty(name = "avatarPath")
  private String avatarPath;
}
