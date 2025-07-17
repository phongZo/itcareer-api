package com.base.auth.form.educator;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Data;

@Data
public class UpdateProfileEducatorForm {
  @ApiModelProperty(name = "username")
  private String username;
  @ApiModelProperty(name = "fullname")
  private String fullname;
  @ApiModelProperty(name = "birthday")
  private Date birthday;
  @ApiModelProperty(name = "avatarPath")
  private String avatarPath;
}
