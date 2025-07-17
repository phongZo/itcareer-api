package com.base.auth.form.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class ApproveEducator {
  @NotNull(message = "id cant not be null")
  @ApiModelProperty(name = "id", required = true)
  private Long id;
  @NotNull(message = "status cant not be null")
  @ApiModelProperty(name = "status", required = true)
  private Integer status;
}
