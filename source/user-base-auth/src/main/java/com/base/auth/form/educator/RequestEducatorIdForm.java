package com.base.auth.form.educator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class RequestEducatorIdForm {
  @NotNull(message = "Id cannot be null")
  @ApiModelProperty(name = "id")
  private Long id;
}
