package com.base.auth.form.review;

import com.base.auth.validation.Star;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel
public class UpdateReviewForm {
  @NotNull(message = "id cannot be null")
  @ApiModelProperty(name = "id")
  private Long id;
  @Star
  @ApiModelProperty(name = "star")
  private Integer star;
  @ApiModelProperty(name = "comment")
  private String comment;
}
