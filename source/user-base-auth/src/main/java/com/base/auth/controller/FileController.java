package com.base.auth.controller;

import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.UploadFileDto;
import com.base.auth.form.UploadFileForm;
import com.base.auth.service.UserBaseApiService;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/file")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class FileController extends ABasicController{
  @Autowired
  private UserBaseApiService userBaseApiService;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('FILE_U')")
  public ApiMessageDto<UploadFileDto> upload(@Valid UploadFileForm request, BindingResult bindingResult){
    return userBaseApiService.storeFile(request);
  }

  @GetMapping("/download/{folder}/{fileName:.+}")
  @Cacheable("images")
  public ResponseEntity<Resource> downloadFile(@PathVariable String folder, @PathVariable String fileName, HttpServletRequest request) throws FileNotFoundException {
    return getResource(folder, fileName, request);
  }

  private ResponseEntity<Resource> getResource(String folder, String fileName, HttpServletRequest request) {
    Resource resource = userBaseApiService.loadFileAsResource(folder, fileName);
    String contentType = null;
    try {
      contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath()); // Lấy loại ảnh là gì .jpg, .png, ...
    } catch (IOException ex) {
      log.info("Could not determine file type.");
    }
    if (contentType == null) {
      contentType = "application/octet-stream";
    }
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(7776000, TimeUnit.SECONDS)) // Cho phép trình duyệt nhớ file này 90 ngày
        .contentType(MediaType.parseMediaType(contentType)) // Nói rõ đây là thể loại gì
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"") // Đính kèm file để trình duyệt tải xuống
        .body(resource); // Gửi file về
  }
}
