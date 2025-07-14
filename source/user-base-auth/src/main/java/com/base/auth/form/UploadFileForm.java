package com.base.auth.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@ApiModel
public class UploadFileForm {
  @NotNull(message = "file cant not be null")
  @ApiModelProperty(name = "file", required = true)
  private MultipartFile file;
  @NotEmpty(message = "type cant not be null")
  @ApiModelProperty(name = "type",value = "the type of file: LOGO, AVATAR, IMAGE", required = true)
  private String type;
}
