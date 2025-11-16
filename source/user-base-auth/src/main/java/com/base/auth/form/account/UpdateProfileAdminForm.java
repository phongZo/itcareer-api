package com.base.auth.form.account;

import com.base.auth.validation.Email;
import com.base.auth.validation.Password;
import com.base.auth.validation.Phone;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.validation.constraints.Past;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel
public class UpdateProfileAdminForm {
    @Email
    @ApiModelProperty(name = "email")
    private String email;
    @Phone
    @ApiModelProperty(name = "phone")
    private String phone;
    @Password
    @ApiModelProperty(name = "password")
    private String password;
    @ApiModelProperty(name = "oldPassword", required = true)
    @NotEmpty(message = "oldPassword is required")
    private String oldPassword;
    @NotEmpty(message = "fullName is required")
    @ApiModelProperty(name = "fullName", required = true)
    private String fullName;
    @ApiModelProperty(name = "birthday")
    @Past(message = "birthday must be in the past")
    private Date birthday;
    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;
}
