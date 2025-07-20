package com.base.auth.controller;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.jwt.UserBaseJwt;
import com.base.auth.model.Account;
import com.base.auth.model.Educator;
import com.base.auth.model.Student;
import com.base.auth.repository.EducatorRepository;
import com.base.auth.repository.SpecializationRepository;
import com.base.auth.repository.StudentRepository;
import com.base.auth.service.CommonAsyncService;
import com.base.auth.service.UserBaseApiService;
import com.base.auth.service.impl.UserServiceImpl;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.Objects;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class ABasicController {
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    EducatorRepository educatorRepository;

    @Autowired
    SpecializationRepository specializationRepository;

    @Autowired
    private UserBaseApiService userBaseApiService;

    public long getCurrentUser(){
        UserBaseJwt userBaseJwt = userService.getAddInfoFromToken();
        return userBaseJwt.getAccountId();
    }

    public long getTokenId(){
        UserBaseJwt userBaseJwt = userService.getAddInfoFromToken();
        return userBaseJwt.getTokenId();
    }

    public UserBaseJwt getSessionFromToken(){
        return userService.getAddInfoFromToken();
    }

    public boolean isSuperAdmin(){
        UserBaseJwt userBaseJwt = userService.getAddInfoFromToken();
        if(userBaseJwt !=null){
            return userBaseJwt.getIsSuperAdmin();
        }
        return false;
    }

    public boolean isShop(){
        UserBaseJwt userBaseJwt = userService.getAddInfoFromToken();
        if(userBaseJwt !=null){
            return Objects.equals(userBaseJwt.getUserKind(), UserBaseConstant.USER_KIND_MANAGER);
        }
        return false;
    }

    public String getCurrentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            OAuth2AuthenticationDetails oauthDetails =
                    (OAuth2AuthenticationDetails) authentication.getDetails();
            if (oauthDetails != null) {
                return oauthDetails.getTokenValue();
            }
        }
        return null;
    }

    public Boolean isStudent(){
        if (!studentRepository.existsByAccountId(getCurrentUser())){
            return false;
        }
        return true;
    }

    public Boolean isEducator(){
        if (!educatorRepository.existsByAccountId(getCurrentUser())){
            return false;
        }
        return true;
    }

    public Boolean isSpecialization(Long id){
        if (!specializationRepository.existsById(id)){
            return false;
        }
        return true;
    }

    protected void sendVerifyAccount(Account user){
        String subject = "Xác thực tài khoản";
        String html = "<div style=\"font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 8px; background-color: #f9f9f9;\">" +
            "<h2 style=\"color: #2c3e50; text-align: center;\">Chào bạn!</h2>" +
            "<p style=\"font-size: 16px; line-height: 1.6;\">Bạn đã đăng ký tài khoản thành công. Mã OTP của bạn là:</p>" +
            "<p style=\"font-size: 18px; font-weight: bold; text-align: center; color: #e74c3c; background-color: #fff; border: 1px dashed #e74c3c; padding: 10px; border-radius: 4px;\">" + user.getResetPwdCode() + "</p>" +
            "<p style=\"font-size: 16px; line-height: 1.6;\">Vui lòng nhập mã này vào trang xác thực để kích hoạt tài khoản.</p>" +
            "<p style=\"font-size: 16px; line-height: 1.6; color: #555;\">Mã OTP có hiệu lực trong <strong>5 phút</strong>.</p>" +
            "<p style=\"font-size: 14px; color: #999; font-style: italic;\">Nếu bạn không thực hiện hành động này, hãy bỏ qua email này.</p>" +
            "</div>";
        userBaseApiService.sendEmail(user.getEmail(), html, subject, true);
    }

    protected void reSendVerifyAccount(Account user){
        String subject = "Xác thực tài khoản";
        String html = "<div style=\"font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 8px; background-color: #f9f9f9;\">" +
            "<h2 style=\"color: #2c3e50; text-align: center;\">Chào bạn!</h2>" +
            "<p style=\"font-size: 16px; line-height: 1.6;\">Bạn vừa yêu cầu gửi lại mã OTP. Mã mới của bạn là:</p>" +
            "<p style=\"font-size: 18px; font-weight: bold; text-align: center; color: #e74c3c; background-color: #fff; border: 1px dashed #e74c3c; padding: 10px; border-radius: 4px;\">" + user.getResetPwdCode() + "</p>" +
            "<p style=\"font-size: 16px; line-height: 1.6;\">Vui lòng nhập mã này vào trang xác thực để kích hoạt tài khoản.</p>" +
            "<p style=\"font-size: 16px; line-height: 1.6; color: #555;\">Mã OTP có hiệu lực trong <strong>5 phút</strong>.</p>" +
            "<p style=\"font-size: 14px; color: #999; font-style: italic;\">Nếu bạn không thực hiện hành động này, hãy bỏ qua email này.</p>" +
            "</div>";
        userBaseApiService.sendEmail(user.getEmail(), html, subject, true);
    }
}
