package com.base.auth.service;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.model.Permission;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    public void deleteByFilePath(String filePath) {
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                log.warn("======> Empty path provided, skip delete");
                return;
            }

            String cleanedPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
            String[] parts = cleanedPath.split("/", 2);

            if (parts.length < 2) {
                log.warn("======> Invalid path format: {}", filePath);
                return;
            }

            String rootFolder = parts[0]; // video, avatar, image, document...
            String subPath = parts[1];
            String basePath = uploadDir + ITDreamConstant.DIRECTORY_GENERAL + "/" + rootFolder;
            Path subPathObj = Paths.get(subPath);
            boolean isFolderKind = !subPathObj.getFileName().toString().contains(".");

            if (isFolderKind) {
                String folderName = subPathObj.getName(0).toString();
                File targetFolder = new File(basePath + "/" + folderName);
                log.info("======> Deleting folder: {}", targetFolder.getAbsolutePath());
                if (targetFolder.exists() && targetFolder.isDirectory()) {
                    deleteDirectory(targetFolder.toPath());
                    log.info("======> Folder '{}' deleted successfully", targetFolder.getAbsolutePath());
                } else {
                    log.warn("======> Folder not found or not a directory: {}", targetFolder.getAbsolutePath());
                }
            } else {
                File targetFile = new File(basePath + "/" + subPath);
                log.info("======> Deleting file: {}", targetFile.getAbsolutePath());
                if (targetFile.exists() && targetFile.isFile()) {
                    if (targetFile.delete()) {
                        log.info("======> File '{}' deleted successfully", targetFile.getAbsolutePath());
                    } else {
                        log.warn("======> Failed to delete file: {}", targetFile.getAbsolutePath());
                    }
                } else {
                    log.warn("======> File not found or is not a file: {}", targetFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            log.error("======> Error occurred while deleting path: {}", filePath, e);
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
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
