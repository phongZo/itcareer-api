package com.base.auth.form.specialization;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@ApiModel
public class CreateSpecializationForm {
  @NotEmpty(message = "name cannot be null")
  @ApiModelProperty(name = "name")
  private String name;
}
