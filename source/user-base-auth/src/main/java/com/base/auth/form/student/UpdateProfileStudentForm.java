package com.base.auth.form.student;

import com.base.auth.validation.Phone;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Past;
import lombok.Data;

@Data
@ApiModel
public class UpdateProfileStudentForm {
  @ApiModelProperty(name = "username")
  @NotEmpty(message = "username cannot be null")
  private String username;
  @ApiModelProperty(name = "fullname")
  @NotEmpty(message = "username cannot be null")
  private String fullname;
  @ApiModelProperty(name = "birthday")
  @Past(message = "birthday must be in the past")
  private Date birthday;
  @Phone
  private String phone;
  @ApiModelProperty(name = "avatarPath")
  private String avatarPath;
}
