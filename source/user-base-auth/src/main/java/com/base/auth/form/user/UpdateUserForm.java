package com.base.auth.form.user;

import com.base.auth.validation.Email;
import com.base.auth.validation.Password;
import com.base.auth.validation.Phone;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class UpdateUserForm {
    @NotNull(message = "id cant not be null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;
    @ApiModelProperty(name = "username")
    private String username;
    @ApiModelProperty(name = "name")
    private String fullName;
    @Phone(allowNull = true)
    @ApiModelProperty(name = "phone")
    private String phone;
    @Email(allowNull = true)
    @ApiModelProperty(name = "email")
    private String email;
    @Password(allowNull = true)
    @ApiModelProperty(name = "password")
    private String password;
    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;
}
