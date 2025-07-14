package com.base.auth.form.user;

import com.base.auth.validation.Email;
import com.base.auth.validation.Password;
import com.base.auth.validation.Phone;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@ApiModel
public class SignUpUserForm {
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
    @ApiModelProperty(name = "kind")
    @NotNull(message = "kind cant not be null")
    private int kind;
}
