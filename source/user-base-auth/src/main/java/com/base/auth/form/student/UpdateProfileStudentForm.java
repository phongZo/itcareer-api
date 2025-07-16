package com.base.auth.form.student;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Data;

@Data
@ApiModel
public class UpdateProfileStudentForm {
  @ApiModelProperty(name = "username")
  private String username;
  @ApiModelProperty(name = "fullname")
  private String fullname;
  @ApiModelProperty(name = "birthday")
  private Date birthday;
  @ApiModelProperty(name = "avatarPath")
  private String avatarPath;
}
