package com.base.auth.form.educator;

import com.base.auth.validation.Email;
import com.base.auth.validation.Password;
import com.base.auth.validation.Phone;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import lombok.Data;

@Data
public class UpdateEducatorForm {
  @NotNull(message = "id cant not be null")
  @ApiModelProperty(name = "id", required = true)
  private Long id;
  @ApiModelProperty(name = "username")
  @NotEmpty(message = "username cannot be null")
  private String username;
  @ApiModelProperty(name = "fulName")
  @NotEmpty(message = "fullName cannot be null")
  private String fullName;
  @Phone
  @ApiModelProperty(name = "phone")
  private String phone;
  @Email
  @ApiModelProperty(name = "email")
  private String email;
  @Password(allowNull = true)
  @ApiModelProperty(name = "password")
  private String password;
  @ApiModelProperty(name = "birthday")
  @Past(message = "birthday must be in the past")
  private Date birthday;
  @ApiModelProperty(name = "avatarPath")
  private String avatarPath;
}
