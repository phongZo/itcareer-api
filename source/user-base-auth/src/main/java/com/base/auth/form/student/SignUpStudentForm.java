package com.base.auth.form.student;

import com.base.auth.validation.Email;
import com.base.auth.validation.Password;
import com.base.auth.validation.Phone;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Past;
import lombok.Data;

@Data
@ApiModel
public class SignUpStudentForm {
  @ApiModelProperty(name = "username", required = true)
  @NotEmpty(message = "username cant not be null")
  private String username;
  @ApiModelProperty(name = "email")
  @Email
  private String email;
  @ApiModelProperty(name = "phone",required = true)
  @Phone
  private String phone;
  @ApiModelProperty(name = "password", required = true)
  @Password
  private String password;
  @NotEmpty(message = "fullName cant not be null")
  @ApiModelProperty(name = "fullName",example = "Tam Nguyen",required = true)
  private String fullName;
  @ApiModelProperty(name = "birthday")
  @Past(message = "birthday must be in the past")
  private Date birthday;
}
