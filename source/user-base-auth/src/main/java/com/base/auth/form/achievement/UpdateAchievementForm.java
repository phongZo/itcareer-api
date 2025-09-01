package com.base.auth.form.achievement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class UpdateAchievementForm {
  @NotNull(message = "id cannot be null")
  @ApiModelProperty(name = "id")
  private Long id;
  @NotEmpty(message = "filePath cannot be null")
  @ApiModelProperty(name = "filePath")
  private String filePath;
}
