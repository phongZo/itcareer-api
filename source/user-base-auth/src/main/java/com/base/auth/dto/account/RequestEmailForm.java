package com.base.auth.dto.account;

import com.base.auth.validation.Email;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class RequestEmailForm {
    @Email
    @ApiModelProperty(name = "email", required = true)
    private String email;
}
