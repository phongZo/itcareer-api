package com.base.auth.controller;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.account.OtpDto;
import com.base.auth.dto.account.RequestEmailForm;
import com.base.auth.dto.user.ProfileUserDto;
import com.base.auth.dto.user.UserAutoCompleteDto;
import com.base.auth.dto.user.UserDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.user.SignUpUserForm;
import com.base.auth.form.user.UpdateProfileUserForm;
import com.base.auth.form.user.UpdateUserForm;
import com.base.auth.form.user.VerifyUserForm;
import com.base.auth.mapper.AccountMapper;
import com.base.auth.mapper.UserMapper;
import com.base.auth.model.Account;
import com.base.auth.model.Group;
import com.base.auth.model.User;
import com.base.auth.model.criteria.UserCriteria;
import com.base.auth.repository.*;
import com.base.auth.service.CommonAsyncService;
import com.base.auth.service.UserBaseApiService;
import com.base.auth.utils.AESUtils;
import com.base.auth.utils.ConvertUtils;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class UserController extends ABasicController{

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserBaseApiService userBaseApiService;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CommonAsyncService commonAsyncService;

    @PostMapping(value = "/signup", produces= MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<OtpDto> create(@Valid @RequestBody SignUpUserForm signUpUserForm, BindingResult bindingResult)
    {
        ApiMessageDto<OtpDto> apiMessageDto = new ApiMessageDto<>();

        Account accountByUsername = accountRepository.findAccountByUsername(signUpUserForm.getUsername());
        if (accountByUsername != null){
            throw new BadRequestException("username already exists", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
        }

        Account accountByPhone = accountRepository.findAccountByPhone(signUpUserForm.getPhone());
        if (accountByPhone!=null)
        {
            throw new BadRequestException("phone already exists", ErrorCode.ACCOUNT_ERROR_PHONE_EXIST);
        }

        Account accountByEmail = accountRepository.findAccountByEmail(signUpUserForm.getEmail());
        if (accountByEmail!=null)
        {
            throw new BadRequestException("email already exists", ErrorCode.ACCOUNT_ERROR_EMAIL_EXIST);
        }

        Account account = accountMapper.fromSignUpUserToAccount(signUpUserForm);
        account.setPassword(passwordEncoder.encode(signUpUserForm.getPassword()));
        int grant = UserBaseConstant.USER_KIND_ENTERISE.equals(signUpUserForm.getKind()) ? UserBaseConstant.USER_KIND_ENTERISE
                : UserBaseConstant.USER_KIND_EDUCATOR.equals(signUpUserForm.getKind()) ? UserBaseConstant.USER_KIND_EDUCATOR
                :UserBaseConstant.USER_KIND_STUDENT;
        account.setKind(grant);
        Group group = groupRepository.findFirstByKind(grant);
        account.setGroup(group);
        account.setStatus(UserBaseConstant.STATUS_PENDING);
        String otp = userBaseApiService.getRequestOTP();
        account.setAttemptCode(0);
        account.setResetPwdCode(otp);
        account.setResetPwdTime(new Date());
        accountRepository.save(account);

        User user = new User();
        user.setAccount(account);
        user.setBirthday(signUpUserForm.getBirthday());
        user.setStatus(UserBaseConstant.STATUS_PENDING);
        userRepository.save(user);

        sendVerifyAccount(account);
        OtpDto otpDto = new OtpDto();
        String hash = AESUtils.encrypt (account.getId()+";"+otp, true);
        otpDto.setIdHash(hash);

        apiMessageDto.setResult(true);
        apiMessageDto.setData(otpDto);
        apiMessageDto.setMessage("Sign Up Success, please check email.");
        return apiMessageDto;
    }

    @GetMapping(value = "/get/{id}", produces= MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('US_V')")
    public ApiMessageDto<UserDto> getUser(@PathVariable("id") Long id)
    {
        ApiMessageDto<UserDto> apiMessageDto = new ApiMessageDto<>();
        User user = userRepository.findById(id).orElse(null);
        if (user==null)
        {
            apiMessageDto.setResult(false);
            apiMessageDto.setMessage("Not found user");
            apiMessageDto.setCode(ErrorCode.USER_ERROR_NOT_FOUND);
            return apiMessageDto;
        }
        apiMessageDto.setData(userMapper.fromEntityToUserDto(user));
        apiMessageDto.setMessage("get user success");
        return apiMessageDto;
    }

    @DeleteMapping(value = "/delete/{id}")
    @PreAuthorize("hasRole('US_D')")
    public ApiMessageDto<String> deleteUser(@PathVariable("id") Long id)
    {
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        User user = userRepository.findById(id).orElse(null);
        if (user==null)
        {
            apiMessageDto.setResult(false);
            apiMessageDto.setMessage("Not found user");
            apiMessageDto.setCode(ErrorCode.USER_ERROR_NOT_FOUND);
            return apiMessageDto;
        }
        Account account = accountRepository.findById(user.getAccount().getId()).orElse(null);
        if (account.getIsSuperAdmin()){
            apiMessageDto.setResult(false);
            apiMessageDto.setMessage("Not allow delete super admin");
            apiMessageDto.setCode(ErrorCode.ACCOUNT_ERROR_NOT_ALLOW_DELETE_SUPPER_ADMIN);
            return apiMessageDto;
        }
        addressRepository.deleteAllByUserId(id);
        userRepository.delete(user);
        accountRepository.delete(account);
        apiMessageDto.setMessage("Delete User success");
        return apiMessageDto;
    }

    @GetMapping(value = "/list", produces= MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('US_L')")
    public ApiMessageDto<ResponseListDto<List<UserDto>>> getList(UserCriteria userCriteria , Pageable pageable)
    {
        ApiMessageDto<ResponseListDto<List<UserDto>>> apiMessageDto = new ApiMessageDto<>();
        ResponseListDto<List<UserDto>> responseListDto = new ResponseListDto<>();
        Page<User> listUser = userRepository.findAll(userCriteria.getSpecification(),pageable);
        responseListDto.setContent(userMapper.fromUserListToUserDtoList(listUser.getContent()));
        responseListDto.setTotalPages(listUser.getTotalPages());
        responseListDto.setTotalElements(listUser.getTotalElements());

        apiMessageDto.setData(responseListDto);
        apiMessageDto.setMessage("Get list user success");
        return apiMessageDto;
    }

    @GetMapping(value = "/auto-complete",produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<UserAutoCompleteDto>>> ListAutoComplete(UserCriteria userCriteria)
    {
        ApiMessageDto<ResponseListDto<List<UserAutoCompleteDto>>> apiMessageDto = new ApiMessageDto<>();
        ResponseListDto<List<UserAutoCompleteDto>> responseListDto = new ResponseListDto<>();
        Pageable pageable = PageRequest.of(0,10);
        Page<User> listUser =userRepository.findAll(userCriteria.getSpecification(),pageable);
        responseListDto.setContent(userMapper.fromUserListToUserDtoListAutocomplete(listUser.getContent()));
        responseListDto.setTotalPages(listUser.getTotalPages());
        responseListDto.setTotalElements(listUser.getTotalElements());

        apiMessageDto.setData(responseListDto);
        apiMessageDto.setMessage("get success");
        return apiMessageDto;
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('US_U')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateUserForm updateUserForm, BindingResult bindingResult) {

        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        User user = userRepository.findById(updateUserForm.getId()).orElse(null);
        if (user==null)
        {
            throw new NotFoundException("user not found", ErrorCode.USER_ERROR_NOT_FOUND);
        }

        Account account = accountRepository.findById(user.getAccount().getId()).orElse(null);
        if (account == null){
            throw new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }

        if (StringUtils.isNotBlank(updateUserForm.getUsername())){
            if (!Objects.equals(user.getAccount().getUsername(), updateUserForm.getUsername())){
                Account accountByUsername = accountRepository.findAccountByUsername(updateUserForm.getUsername());
                if (accountByUsername != null){
                    throw new BadRequestException("username already exist", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
                }
                account.setUsername(updateUserForm.getUsername());
            }
        }

        if (StringUtils.isNotBlank(updateUserForm.getPhone())){
            if (!Objects.equals(user.getAccount().getPhone(), updateUserForm.getPhone()))
            {
                Account accountByPhone = accountRepository.findAccountByPhone(updateUserForm.getPhone());
                if(accountByPhone!=null)
                {
                    throw new BadRequestException("phone already exist", ErrorCode.ACCOUNT_ERROR_PHONE_EXIST);
                }
                account.setPhone(updateUserForm.getPhone());
            }
        }

        if (StringUtils.isNotBlank(updateUserForm.getEmail())){
            if (!Objects.equals(user.getAccount().getEmail(), updateUserForm.getEmail()))
            {
                Account accountByEmail = accountRepository.findAccountByEmail(updateUserForm.getEmail());
                if(accountByEmail!=null)
                {
                    throw new BadRequestException("email already exist", ErrorCode.ACCOUNT_ERROR_EMAIL_EXIST);
                }
                account.setEmail(updateUserForm.getEmail());
            }
        }

        if(StringUtils.isNoneBlank(updateUserForm.getPassword()))
        {
            account.setPassword(passwordEncoder.encode(updateUserForm.getPassword()));
        }

        if (StringUtils.isNotBlank(updateUserForm.getAvatarPath())) {
            if (!updateUserForm.getAvatarPath().equals(account.getAvatarPath())){
                userBaseApiService.deleteFile(account.getAvatarPath());
            }
            account.setAvatarPath(updateUserForm.getAvatarPath());
        }

        accountMapper.fromUpdateUserFormToEntity(updateUserForm, account);
        accountRepository.save(account);
        apiMessageDto.setMessage("update success");
        return apiMessageDto;
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('US_U_P')")
    public ApiMessageDto<ProfileUserDto> getProfile(){
        ApiMessageDto<ProfileUserDto> apiMessageDto = new ApiMessageDto<>();
        Account account = accountRepository.findById(getCurrentUser()).orElseThrow(
            () -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        User user = userRepository.findByAccountId(account.getId()).orElseThrow(
            () -> new NotFoundException("user not found", ErrorCode.USER_ERROR_NOT_FOUND));
        ProfileUserDto userDto = userMapper.fromUserToProfileDto(user);
        apiMessageDto.setData(userDto);
        apiMessageDto.setMessage("Get profile success");
        return apiMessageDto;
    }

    @PutMapping(value = "/client_update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('US_U_U')")
    public ApiMessageDto<String> updateProfile(@Valid @RequestBody UpdateProfileUserForm request, BindingResult bindingResult){
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Account currentAccount = accountRepository.findById(getCurrentUser()).orElseThrow(() ->
            new NotFoundException("account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        User currentUser = userRepository.findByAccountId(currentAccount.getId()).orElseThrow(() ->
            new NotFoundException("user not found", ErrorCode.USER_ERROR_NOT_FOUND));

        if (StringUtils.isNotBlank(request.getUsername())){
            if (!Objects.equals(currentAccount.getUsername(), request.getUsername())){
                Account accountByUsername = accountRepository.findAccountByUsername(request.getUsername());
                if (accountByUsername != null){
                    throw new BadRequestException("username already exists", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
                }
                currentAccount.setUsername(request.getUsername());
            }
        }

        accountMapper.fromUpdateProfileUserFormToEntity(request, currentAccount);
        userMapper.fromUpdateProfileUserFormToEntity(request, currentUser);
        currentUser.setAccount(currentAccount);
        accountRepository.save(currentAccount);
        userRepository.save(currentUser);
        apiMessageDto.setMessage("update profile user success");
        return apiMessageDto;
    }

    @PostMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> verifyAccount(@RequestBody @Valid VerifyUserForm verifyUserForm){
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        String[] hash = AESUtils.decrypt(verifyUserForm.getIdHash(),true).split(";",2);
        Long id = ConvertUtils.convertStringToLong(hash[0]);
        if(id <= 0){
            throw new BadRequestException("incorrect hash verification", ErrorCode.ACCOUNT_ERROR_INCORRECT_HASH_VERIFICATION);
        }

        Account account = accountRepository.findById(id).orElse(null);
        if (account == null ) {
            throw new NotFoundException("account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }

        if(account.getAttemptCode() >= UserBaseConstant.MAX_ATTEMPT_FORGET_PWD){
            account.setStatus(UserBaseConstant.STATUS_LOCK);
            throw new BadRequestException("account has been locked", ErrorCode.ACCOUNT_ERROR_LOCKED);
        }

        if(!account.getResetPwdCode().equals(verifyUserForm.getOtp()) ||
            (new Date().getTime() - account.getResetPwdTime().getTime() >= UserBaseConstant.MAX_TIME_FORGET_PWD)){

            //tang so lan
            account.setAttemptCode(account.getAttemptCode()+1);
            accountRepository.save(account);

            apiMessageDto.setResult(false);
            apiMessageDto.setCode(ErrorCode.ACCOUNT_ERROR_OPT_INVALID);
            return apiMessageDto;
        }

        account.setResetPwdTime(null);
        account.setResetPwdCode(null);
        account.setAttemptCode(null);
        account.setStatus(UserBaseConstant.STATUS_ACTIVE);
        accountRepository.save(account);

        User user = userRepository.findByAccountId(id).orElse(null);
        if (user == null){
            throw new NotFoundException("user not found", ErrorCode.USER_ERROR_NOT_FOUND);
        }
        user.setStatus(UserBaseConstant.STATUS_ACTIVE);
        if (!UserBaseConstant.STATUS_ACTIVE.equals(account.getStatus())){
            user.setStatus(UserBaseConstant.STATUS_LOCK);
        }
        userRepository.save(user);
        apiMessageDto.setMessage("verify account success");
        return apiMessageDto;
    }

    @PostMapping(value = "/resend-verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<OtpDto> resendVerification(@Valid @RequestBody RequestEmailForm emailForm, BindingResult bindingResult){
        ApiMessageDto<OtpDto> apiMessageDto = new ApiMessageDto<>();

        Account account = accountRepository.findAccountByEmail(emailForm.getEmail());
        if (account == null) {
            throw new NotFoundException("account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }

        User user = userRepository.findByAccountId(account.getId()).orElse(null);
        if (user == null) {
            throw new NotFoundException("user not found", ErrorCode.USER_ERROR_NOT_FOUND);
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

        apiMessageDto.setResult(true);
        apiMessageDto.setData(otpDto);
        apiMessageDto.setMessage("resend verify email success");
        return apiMessageDto;
    }

    private void sendVerifyAccount(Account user){
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

    private void reSendVerifyAccount(Account user){
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
