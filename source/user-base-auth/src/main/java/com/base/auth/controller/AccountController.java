package com.base.auth.controller;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.account.AccountDto;
import com.base.auth.dto.account.OtpDto;
import com.base.auth.dto.account.ProfileAccountDto;
import com.base.auth.dto.account.RequestEmailForm;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.exception.UnauthorizationException;
import com.base.auth.form.account.CreateAccountAdminForm;
import com.base.auth.form.account.ForgetPasswordForm;
import com.base.auth.form.account.UpdateAccountAdminForm;
import com.base.auth.form.account.UpdateProfileAdminForm;
import com.base.auth.mapper.AccountMapper;
import com.base.auth.model.Account;
import com.base.auth.model.Group;
import com.base.auth.model.criteria.AccountCriteria;
import com.base.auth.repository.*;
import com.base.auth.utils.AESUtils;
import com.base.auth.utils.ConvertUtils;
import com.base.auth.dto.ErrorCode;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("/v1/account")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AccountController extends ABasicController{
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    GroupRepository groupRepository;

    @Autowired
    AccountMapper accountMapper;

    @PostMapping(value = "/create_admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_C')")
    public ApiMessageDto<String> createAdmin(@Valid @RequestBody CreateAccountAdminForm createAccountAdminForm, BindingResult bindingResult) {
        if (!isSuperAdmin()){
            throw new UnauthorizationException("User is not a super admin");
        }
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Boolean existUsername = accountRepository.existsByUsername(createAccountAdminForm.getUsername());
        if (existUsername) {
            throw new BadRequestException("Username already exist", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
        }
        Group group = groupRepository.findById(createAccountAdminForm.getGroupId()).orElseThrow(()
        -> new NotFoundException("Group not found", ErrorCode.GROUP_ERROR_NOT_FOUND));
        
        Account account = accountMapper.fromCreateAccountAdmiFormToAccount(createAccountAdminForm);
        account.setPassword(passwordEncoder.encode(createAccountAdminForm.getPassword()));
        account.setKind(ITDreamConstant.USER_KIND_ADMIN);
        account.setGroup(group);
        accountRepository.save(account);
        apiMessageDto.setMessage("Create account admin success");
        return apiMessageDto;
    }

    @PutMapping(value = "/update_admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_U')")
    public ApiMessageDto<String> updateAdmin(@Valid @RequestBody UpdateAccountAdminForm updateAccountAdminForm, BindingResult bindingResult) {
        if (!isSuperAdmin()){
            throw new UnauthorizationException("User is not a super admin");
        }
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Account account = accountRepository.findById(updateAccountAdminForm.getId()).orElseThrow(()
        -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        Group group = groupRepository.findById(updateAccountAdminForm.getGroupId()).orElseThrow(()
            -> new NotFoundException("Group not found", ErrorCode.GROUP_ERROR_NOT_FOUND));

        if (!Objects.equals(account.getEmail(), updateAccountAdminForm.getEmail())){
            Boolean existEmail = accountRepository.existsByEmail(updateAccountAdminForm.getEmail());
            if (existEmail){
                throw new BadRequestException("Email already exist", ErrorCode.ACCOUNT_ERROR_EMAIL_EXIST);
            }
            account.setEmail(updateAccountAdminForm.getEmail());
        }
        
        if (!Objects.equals(account.getPhone(), updateAccountAdminForm.getPhone())){
            Boolean existPhone = accountRepository.existsByPhone(updateAccountAdminForm.getPhone());
            if (existPhone){
                throw new BadRequestException("Phone already exist", ErrorCode.ACCOUNT_ERROR_PHONE_EXIST);
            }
            account.setPhone(updateAccountAdminForm.getPhone());
        }
        
        if (StringUtils.isNoneBlank(updateAccountAdminForm.getPassword())) {
            account.setPassword(passwordEncoder.encode(updateAccountAdminForm.getPassword()));
        }
        
        if (StringUtils.isNoneBlank(updateAccountAdminForm.getAvatarPath())) {
            if(!updateAccountAdminForm.getAvatarPath().equals(account.getAvatarPath())){
                //delete old image
                userBaseApiService.deleteByFilePath(account.getAvatarPath());
                account.setAvatarPath(updateAccountAdminForm.getAvatarPath());
            }
        }
        
        account.setGroup(group);
        accountMapper.fromUpdateAccountAdminFormToAccount(updateAccountAdminForm, account);
        accountRepository.save(account);
        apiMessageDto.setMessage("Update account admin success");
        return apiMessageDto;

    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_L')")
    public ApiMessageDto<ResponseListDto<List<AccountDto>>> listDtoApiMessageDto(AccountCriteria criteria, Pageable pageable){
        ApiMessageDto<ResponseListDto<List<AccountDto>>> apiMessageDto = new ApiMessageDto<>();
        ResponseListDto<List<AccountDto>> responseListDto = new ResponseListDto<>();
        Page<Account> accounts = accountRepository.findAll(criteria.getSpecification(), pageable);
        List<AccountDto> accountDtos = accountMapper.fromAccountToDtoList(accounts.getContent());
        responseListDto.setContent(accountDtos);
        responseListDto.setTotalElements(accounts.getTotalElements());
        responseListDto.setTotalPages(accounts.getTotalPages());
        apiMessageDto.setData(responseListDto);
        apiMessageDto.setMessage("Get list admin success");
        return apiMessageDto;
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_V')")
    public ApiMessageDto<AccountDto> get(@PathVariable("id") Long id) {
        if (!isSuperAdmin()){
            throw new UnauthorizationException("User is not a super admin");
        }
        ApiMessageDto<AccountDto> apiMessageDto = new ApiMessageDto<>();
        Account account = accountRepository.findById(id).orElseThrow(()
            -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        AccountDto accountDto = accountMapper.fromAccountToDto(account);
        apiMessageDto.setData(accountDto);
        apiMessageDto.setMessage("Get account admin success");
        return apiMessageDto;
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        if (!isSuperAdmin()){
            throw new UnauthorizationException("User is not a super admin");
        }
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Account account = accountRepository.findById(id).orElseThrow(()
            -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        if (account.getIsSuperAdmin()){
            throw new BadRequestException("Not allow delete super admin", ErrorCode.ACCOUNT_ERROR_NOT_ALLOW_DELETE_SUPPER_ADMIN);
        }
        //delete avatar file
        userBaseApiService.deleteByFilePath(account.getAvatarPath());
        accountRepository.deleteById(id);
        apiMessageDto.setMessage("Delete account success");
        return apiMessageDto;
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_AD_P')")
    public ApiMessageDto<ProfileAccountDto> profile() {
        if (!isAdmin()){
            throw new BadRequestException("User is not an admin");
        }
        ApiMessageDto<ProfileAccountDto> apiMessageDto = new ApiMessageDto<>();
        Account account = accountRepository.findById(getCurrentUser()).orElseThrow(()
            -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        apiMessageDto.setData(accountMapper.fromAccountToProfileDto(account));
        apiMessageDto.setMessage("Get account profile success");
        return apiMessageDto;
    }

    @PutMapping(value = "/update_profile_admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_AD_U')")
    public ApiMessageDto<String> updateProfileAdmin(@Valid @RequestBody UpdateProfileAdminForm updateProfileAdminForm, BindingResult bindingResult) {
        if (!isAdmin()){
            throw new BadRequestException("User is not an admin");
        }
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Account account = accountRepository.findById(getCurrentUser()).orElseThrow(()
            -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        if (!Objects.equals(account.getEmail(), updateProfileAdminForm.getEmail())){
            Boolean existEmail = accountRepository.existsByEmail(updateProfileAdminForm.getEmail());
            if (existEmail){
                throw new BadRequestException("Email already exist", ErrorCode.ACCOUNT_ERROR_EMAIL_EXIST);
            }
            account.setEmail(updateProfileAdminForm.getEmail());
        }

        if (!Objects.equals(account.getPhone(), updateProfileAdminForm.getPhone())){
            Boolean existPhone = accountRepository.existsByPhone(updateProfileAdminForm.getPhone());
            if (existPhone){
                throw new BadRequestException("Phone already exist", ErrorCode.ACCOUNT_ERROR_PHONE_EXIST);
            }
            account.setPhone(updateProfileAdminForm.getPhone());
        }

        if(!passwordEncoder.matches(updateProfileAdminForm.getOldPassword(), account.getPassword())){
            throw new BadRequestException("Password invalid", ErrorCode.ACCOUNT_ERROR_WRONG_PASSWORD);
        }

        if (StringUtils.isNoneBlank(updateProfileAdminForm.getPassword())) {
            account.setPassword(passwordEncoder.encode(updateProfileAdminForm.getPassword()));
        }

        if (StringUtils.isNoneBlank(updateProfileAdminForm.getAvatarPath())){
            if (!Objects.equals(account.getAvatarPath(), updateProfileAdminForm.getAvatarPath())){
                userBaseApiService.deleteByFilePath(account.getAvatarPath());
                account.setAvatarPath(updateProfileAdminForm.getAvatarPath());
            }
        }
        accountMapper.fromUpdateProfileAdminFormToAccount(updateProfileAdminForm, account);
        accountRepository.save(account);
        apiMessageDto.setMessage("Update profile admin success");
        return apiMessageDto;

    }

    @PostMapping(value = "/request_forget_password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<OtpDto> requestForgetPassword(@Valid @RequestBody RequestEmailForm emailForm, BindingResult bindingResult){
        ApiMessageDto<OtpDto> apiMessageDto = new ApiMessageDto<>();
        Account account = accountRepository.findAccountByEmail(emailForm.getEmail());
        if (account == null) {
            throw new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }

        String otp = userBaseApiService.getRequestOTP();
        account.setAttemptCode(0);
        account.setResetPwdCode(otp);
        account.setResetPwdTime(new Date());
        account.setStatus(ITDreamConstant.STATUS_PENDING);
        accountRepository.save(account);

        //send email
        sendForgetPassword(account);

        OtpDto otpDto = new OtpDto();
        String hash = AESUtils.encrypt (account.getId()+";"+otp, true);
        otpDto.setIdHash(hash);

        apiMessageDto.setData(otpDto);
        apiMessageDto.setMessage("Request forget password success, please check email.");
        return apiMessageDto;
    }

    @PostMapping(value = "/forget_password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Long> forgetPassword(@Valid @RequestBody ForgetPasswordForm forgetForm, BindingResult bindingResult){
        ApiMessageDto<Long> apiMessageDto = new ApiMessageDto<>();
        String[] hash = AESUtils.decrypt(forgetForm.getIdHash(),true).split(";",2);
        Long id = ConvertUtils.convertStringToLong(hash[0]);
        if(id <= 0){
            throw new BadRequestException("Wrong password hash", ErrorCode.ACCOUNT_ERROR_WRONG_HASH_RESET_PASS);
        }

        Account account = accountRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

        if(account.getAttemptCode() >= ITDreamConstant.MAX_ATTEMPT_FORGET_PWD){
            account.setStatus(ITDreamConstant.STATUS_LOCK);
            accountRepository.save(account);
            throw new BadRequestException("Account has been locked", ErrorCode.ACCOUNT_ERROR_LOCKED);
        }

        if(!account.getResetPwdCode().equals(forgetForm.getOtp()) ||
                (new Date().getTime() - account.getResetPwdTime().getTime() >= ITDreamConstant.MAX_TIME_FORGET_PWD)){

            //tang so lan
            account.setAttemptCode(account.getAttemptCode()+1);
            accountRepository.save(account);
            throw new BadRequestException("OTP invalid", ErrorCode.ACCOUNT_ERROR_OPT_INVALID);
        }

        account.setResetPwdTime(null);
        account.setResetPwdCode(null);
        account.setAttemptCode(null);
        account.setPassword(passwordEncoder.encode(forgetForm.getNewPassword()));
        accountRepository.save(account);
        apiMessageDto.setMessage("Change password success.");
        return apiMessageDto;
    }

    @PostMapping(value = "/resend-verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<OtpDto> resendVerification(@Valid @RequestBody RequestEmailForm emailForm, BindingResult bindingResult){
        ApiMessageDto<OtpDto> apiMessageDto = new ApiMessageDto<>();
        Account account = accountRepository.findAccountByEmail(emailForm.getEmail());
        if (account == null) {
            throw new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }

        if (!Objects.equals(account.getStatus(), ITDreamConstant.STATUS_PENDING)){
            throw new BadRequestException("Account is not pending", ErrorCode.ACCOUNT_ERROR_NOT_PENDING);
        }
        String otp = userBaseApiService.getRequestOTP();
        account.setAttemptCode(0);
        account.setResetPwdCode(otp);
        account.setResetPwdTime(new Date());
        accountRepository.save(account);

        reSendVerifyAccount(account);

        OtpDto otpDto = new OtpDto();
        String hash = AESUtils.encrypt (account.getId()+";"+otp, true);
        otpDto.setIdHash(hash);
        apiMessageDto.setData(otpDto);
        apiMessageDto.setMessage("Resend verify email success");
        return apiMessageDto;
    }

    private void sendForgetPassword(Account user){
        String subject = "Xác thực tài khoản";
        String html = "<div style=\"font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 8px; background-color: #f9f9f9;\">" +
            "<h2 style=\"color: #2c3e50; text-align: center;\">Chào bạn!</h2>" +
            "<p style=\"font-size: 16px; line-height: 1.6;\">Bạn vừa yêu cầu gửi mã OTP cho việc lấy lại mật khẩu. Mã OTP của bạn là:</p>" +
            "<p style=\"font-size: 18px; font-weight: bold; text-align: center; color: #e74c3c; background-color: #fff; border: 1px dashed #e74c3c; padding: 10px; border-radius: 4px;\">" + user.getResetPwdCode() + "</p>" +
            "<p style=\"font-size: 16px; line-height: 1.6;\">Vui lòng nhập mã này vào trang xác thực để khôi phục mật khẩu.</p>" +
            "<p style=\"font-size: 16px; line-height: 1.6; color: #555;\">Mã OTP có hiệu lực trong <strong>5 phút</strong>.</p>" +
            "<p style=\"font-size: 14px; color: #999; font-style: italic;\">Nếu bạn không thực hiện hành động này, hãy bỏ qua email này.</p>" +
            "</div>";
        userBaseApiService.sendEmail(user.getEmail(), html, subject, true);
    }
}
