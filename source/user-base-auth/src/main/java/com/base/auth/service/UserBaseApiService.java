package com.base.auth.service;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.UploadFileDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.form.UploadFileForm;
import com.base.auth.model.Permission;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class UserBaseApiService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    UserBaseOTPService userBaseOTPService;

    @Autowired
    CommonAsyncService commonAsyncService;

    private Map<String, Long> storeQRCodeRandom = new ConcurrentHashMap<>();

    public ApiMessageDto<UploadFileDto> storeFile(UploadFileForm uploadFileForm){
        ApiMessageDto<UploadFileDto> apiMessageDto = new ApiMessageDto<>();
        try {
            boolean contains = Arrays.stream(UserBaseConstant.UPLOAD_TYPES).anyMatch(uploadFileForm.getType()::equalsIgnoreCase);
            if (!contains) {
                throw new BadRequestException("Type is required in LOGO, AVATAR or IMAGE", ErrorCode.FILE_ERROR_UPLOAD_TYPE_INVALID);
            }
            String fileName = StringUtils.cleanPath(uploadFileForm.getFile().getOriginalFilename());
            String extension = FilenameUtils.getExtension(fileName);
            if (uploadFileForm.getType().equals("AVATAR") && !Arrays.stream(UserBaseConstant.AVATAR_EXTENSION).anyMatch(extension::equalsIgnoreCase)) {
                throw new BadRequestException("File format is invalid", ErrorCode.FILE_ERROR_UPLOAD_FORMAT_INVALID);
            }

            String finalFile = uploadFileForm.getType() + "_" + RandomStringUtils.randomAlphanumeric(10) + "." + extension; // tên ảnh
            String typeFolder = File.separator + uploadFileForm.getType(); // Tạo đường dẫn tuyệt đối của folder dựa vào type
            String convertTypeFolder = typeFolder.replace(File.separator, "/");

            Path fileStorageLocation = Paths.get(uploadDir + typeFolder).toAbsolutePath().normalize(); //VD: \\uploads\\image (đường dẫn của windows)
            Files.createDirectories(fileStorageLocation); // Tạo folder
            Path targetLocation = fileStorageLocation.resolve(finalFile); // Xử lý tên ảnh
            Files.copy(uploadFileForm.getFile().getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING); // Lưu ảnh vào
            UploadFileDto uploadFileDto = new UploadFileDto();
            uploadFileDto.setFilePath(convertTypeFolder + "/" + finalFile);
            uploadFileDto.setFileName(fileName);
            uploadFileDto.setExt(extension);
            apiMessageDto.setData(uploadFileDto);
            apiMessageDto.setMessage("Upload file success");
        } catch (IOException e) {
            log.error(e.getMessage());
            apiMessageDto.setResult(false);
            apiMessageDto.setMessage(e.getMessage());
        }
        return apiMessageDto;
    }

    public Resource loadFileAsResource(String folder, String fileName) {
        System.out.println("User.home: " + System.getProperty("spring.config.location"));
        System.out.println("get file: " + folder + "/" + fileName + ", path: " + uploadDir);
        try {
            Path fileStorageLocation = Paths.get(uploadDir + File.separator + folder).toAbsolutePath().normalize();
            Path fP = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(fP.toUri());
            if (resource.exists()) {
                return resource;
            }
        } catch (MalformedURLException ex) {
            log.error(ex.getMessage());
        }
        return null;
    }

    public void deleteFile(String filePath) {
        File file = new File(uploadDir + filePath);
        if (file.exists()){
            file.delete();
        }

    }

    public String getRequestOTP(){
        return userBaseOTPService.generate(6);
    }

    public synchronized Long getOrderHash(){
        return Long.parseLong(userBaseOTPService.generate(9))+System.currentTimeMillis();
    }


    public void sendEmail(String email, String msg, String subject, boolean html){
        commonAsyncService.sendEmail(email,msg,subject,html);
    }


    public String convertGroupToUri(List<Permission> permissions){
        if(permissions!=null){
            StringBuilder builderPermission = new StringBuilder();
            for(Permission p : permissions){
                builderPermission.append(p.getAction().trim().replace("/v1","")+",");
            }
            return  builderPermission.toString();
        }
        return null;
    }

    public String getOrderStt(Long storeId){
        return userBaseOTPService.orderStt(storeId);
    }


    public synchronized boolean checkCodeValid(String code){
        //delelete key has valule > 60s
        Set<String> keys = storeQRCodeRandom.keySet();
        Iterator<String> iterator = keys.iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            Long value = storeQRCodeRandom.get(key);
            if((System.currentTimeMillis() - value) > 60000){
                storeQRCodeRandom.remove(key);
            }
        }

        if(storeQRCodeRandom.containsKey(code)){
            return false;
        }
        storeQRCodeRandom.put(code,System.currentTimeMillis());
        return true;
    }
}
