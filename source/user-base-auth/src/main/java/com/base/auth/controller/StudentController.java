package com.base.auth.controller;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.account.OtpDto;
import com.base.auth.dto.account.RequestEmailForm;
import com.base.auth.dto.student.ProfileStudentDto;
import com.base.auth.dto.student.StudentAutoCompleteDto;
import com.base.auth.dto.student.StudentDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.student.SignUpStudentForm;
import com.base.auth.form.student.UpdateProfileStudentForm;
import com.base.auth.form.student.UpdateStudentForm;
import com.base.auth.form.account.VerifyUserForm;
import com.base.auth.mapper.AccountMapper;
import com.base.auth.mapper.StudentMapper;
import com.base.auth.model.Account;
import com.base.auth.model.Group;
import com.base.auth.model.Student;
import com.base.auth.model.criteria.StudentCriteria;
import com.base.auth.repository.AccountRepository;
import com.base.auth.repository.GroupRepository;
import com.base.auth.repository.StudentRepository;
import com.base.auth.service.CommonAsyncService;
import com.base.auth.service.UserBaseApiService;
import com.base.auth.utils.AESUtils;
import com.base.auth.utils.ConvertUtils;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/student")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class StudentController extends ABasicController{
  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private StudentMapper studentMapper;

  @Autowired
  private AccountMapper accountMapper;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserBaseApiService userBaseApiService;

  @PostMapping(value = "/signup", produces= MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<OtpDto> create(@Valid @RequestBody SignUpStudentForm signUpStudentForm, BindingResult bindingResult)
  {
    ApiMessageDto<OtpDto> apiMessageDto = new ApiMessageDto<>();

    Account accountByUsername = accountRepository.findAccountByUsername(signUpStudentForm.getUsername());
    if (accountByUsername != null){
      throw new BadRequestException("username already exists", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
    }

    Account accountByPhone = accountRepository.findAccountByPhone(signUpStudentForm.getPhone());
    if (accountByPhone!=null)
    {
      throw new BadRequestException("phone already exists", ErrorCode.ACCOUNT_ERROR_PHONE_EXIST);
    }

    Account accountByEmail = accountRepository.findAccountByEmail(signUpStudentForm.getEmail());
    if (accountByEmail!=null)
    {
      throw new BadRequestException("email already exists", ErrorCode.ACCOUNT_ERROR_EMAIL_EXIST);
    }

    Account account = accountMapper.fromSignUpStudentToAccount(signUpStudentForm);
    account.setPassword(passwordEncoder.encode(signUpStudentForm.getPassword()));
    account.setKind(UserBaseConstant.USER_KIND_STUDENT);
    Group group = groupRepository.findFirstByKind(UserBaseConstant.USER_KIND_STUDENT);
    account.setGroup(group);
    account.setStatus(UserBaseConstant.STATUS_PENDING);
    String otp = userBaseApiService.getRequestOTP();
    account.setAttemptCode(0);
    account.setResetPwdCode(otp);
    account.setResetPwdTime(new Date());
    accountRepository.save(account);

    Student student = new Student();
    student.setAccount(account);
    student.setBirthday(signUpStudentForm.getBirthday());
    student.setStatus(UserBaseConstant.STATUS_PENDING);
    studentRepository.save(student);

    sendVerifyAccount(account);
    OtpDto otpDto = new OtpDto();
    String hash = AESUtils.encrypt (account.getId()+";"+otp, true);
    otpDto.setIdHash(hash);

    apiMessageDto.setResult(true);
    apiMessageDto.setData(otpDto);
    apiMessageDto.setMessage("Sign Up Success, please check email.");
    return apiMessageDto;
  }

  @GetMapping(value = "/list", produces= MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ST_L')")
  public ApiMessageDto<ResponseListDto<List<StudentDto>>> getListStudent(StudentCriteria studentCriteria , Pageable pageable)
  {
    ApiMessageDto<ResponseListDto<List<StudentDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<StudentDto>> responseListDto = new ResponseListDto<>();
    Page<Student> listStudent = studentRepository.findAll(studentCriteria.getSpecification(),pageable);
    responseListDto.setContent(studentMapper.fromStudentListToStudentDtoList(listStudent.getContent()));
    responseListDto.setTotalPages(listStudent.getTotalPages());
    responseListDto.setTotalElements(listStudent.getTotalElements());

    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list student success");
    return apiMessageDto;
  }

  @GetMapping(value = "/auto-complete",produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<ResponseListDto<List<StudentAutoCompleteDto>>> ListStudentAutoComplete(StudentCriteria studentCriteria)
  {
    ApiMessageDto<ResponseListDto<List<StudentAutoCompleteDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<StudentAutoCompleteDto>> responseListDto = new ResponseListDto<>();
    Pageable pageable = PageRequest.of(0,10);
    Page<Student> listStudent = studentRepository.findAll(studentCriteria.getSpecification(),pageable);
    responseListDto.setContent(studentMapper.fromStudentListToStudentDtoListAutocomplete(listStudent.getContent()));
    responseListDto.setTotalPages(listStudent.getTotalPages());
    responseListDto.setTotalElements(listStudent.getTotalElements());

    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("get success");
    return apiMessageDto;
  }

  @GetMapping(value = "/get/{id}", produces= MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ST_V')")
  public ApiMessageDto<StudentDto> getStudent(@PathVariable("id") Long id)
  {
    ApiMessageDto<StudentDto> apiMessageDto = new ApiMessageDto<>();
    Student student = studentRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Student not found", ErrorCode.USER_ERROR_NOT_FOUND));
    apiMessageDto.setData(studentMapper.fromEntityToStudentDto(student));
    apiMessageDto.setMessage("get student success");
    return apiMessageDto;
  }

  @Transactional
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ST_U')")
  public ApiMessageDto<String> updateStudent(@Valid @RequestBody UpdateStudentForm updateStudentForm, BindingResult bindingResult) {

    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Student student = studentRepository.findById(updateStudentForm.getId()).orElse(null);
    if (student==null)
    {
      throw new NotFoundException("Student not found", ErrorCode.USER_ERROR_NOT_FOUND);
    }

    Account account = accountRepository.findById(student.getAccount().getId()).orElse(null);
    if (account == null){
      throw new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    if (StringUtils.isNotBlank(updateStudentForm.getUsername())){
      if (!Objects.equals(student.getAccount().getUsername(), updateStudentForm.getUsername())){
        Account accountByUsername = accountRepository.findAccountByUsername(updateStudentForm.getUsername());
        if (accountByUsername != null){
          throw new BadRequestException("username already exist", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
        }
        account.setUsername(updateStudentForm.getUsername());
      }
    }

    if (StringUtils.isNotBlank(updateStudentForm.getPhone())){
      if (!Objects.equals(student.getAccount().getPhone(), updateStudentForm.getPhone()))
      {
        Account accountByPhone = accountRepository.findAccountByPhone(updateStudentForm.getPhone());
        if(accountByPhone!=null)
        {
          throw new BadRequestException("phone already exist", ErrorCode.ACCOUNT_ERROR_PHONE_EXIST);
        }
        account.setPhone(updateStudentForm.getPhone());
      }
    }

    if (StringUtils.isNotBlank(updateStudentForm.getEmail())){
      if (!Objects.equals(student.getAccount().getEmail(), updateStudentForm.getEmail()))
      {
        Account accountByEmail = accountRepository.findAccountByEmail(updateStudentForm.getEmail());
        if(accountByEmail!=null)
        {
          throw new BadRequestException("email already exist", ErrorCode.ACCOUNT_ERROR_EMAIL_EXIST);
        }
        account.setEmail(updateStudentForm.getEmail());
      }
    }

    if(StringUtils.isNoneBlank(updateStudentForm.getPassword()))
    {
      account.setPassword(passwordEncoder.encode(updateStudentForm.getPassword()));
    }

    if (StringUtils.isNotBlank(updateStudentForm.getAvatarPath())) {
      if (!updateStudentForm.getAvatarPath().equals(account.getAvatarPath())){
        userBaseApiService.deleteFile(account.getAvatarPath());
      }
    }

    accountMapper.fromUpdateStudentFormToEntity(updateStudentForm, account);
    accountRepository.save(account);
    apiMessageDto.setMessage("update success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/delete/{id}")
  @PreAuthorize("hasRole('ST_D')")
  public ApiMessageDto<String> deleteStudent(@PathVariable("id") Long id)
  {
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Student student = studentRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Student not found", ErrorCode.USER_ERROR_NOT_FOUND));

    Account account = accountRepository.findById(student.getAccount().getId()).orElseThrow(()
    -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

    if (account.getIsSuperAdmin()){
      apiMessageDto.setResult(false);
      apiMessageDto.setMessage("Not allow delete super admin");
      apiMessageDto.setCode(ErrorCode.ACCOUNT_ERROR_NOT_ALLOW_DELETE_SUPPER_ADMIN);
      return apiMessageDto;
    }

    studentRepository.delete(student);
    accountRepository.delete(account);
    apiMessageDto.setMessage("Delete student success");
    return apiMessageDto;
  }

  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ST_U_P')")
  public ApiMessageDto<ProfileStudentDto> getProfileForStudent(){
    ApiMessageDto<ProfileStudentDto> apiMessageDto = new ApiMessageDto<>();
    Account account = accountRepository.findById(getCurrentUser()).orElseThrow(
        () -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
    Student student = studentRepository.findByAccountId(account.getId()).orElseThrow(
        () -> new NotFoundException("Student not found", ErrorCode.USER_ERROR_NOT_FOUND));
    ProfileStudentDto studentDto = studentMapper.fromStudentToProfileDto(student);
    apiMessageDto.setData(studentDto);
    apiMessageDto.setMessage("Get profile success");
    return apiMessageDto;
  }

  @PutMapping(value = "/client_update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ST_U_U')")
  public ApiMessageDto<String> updateProfileForStudent(@Valid @RequestBody UpdateProfileStudentForm updateStudentForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Account currentAccount = accountRepository.findById(getCurrentUser()).orElseThrow(() ->
        new NotFoundException("account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
    Student currentUser = studentRepository.findByAccountId(currentAccount.getId()).orElseThrow(() ->
        new NotFoundException("student not found", ErrorCode.USER_ERROR_NOT_FOUND));

    if (StringUtils.isNotBlank(updateStudentForm.getUsername())){
      if (!Objects.equals(currentAccount.getUsername(), updateStudentForm.getUsername())){
        Account accountByUsername = accountRepository.findAccountByUsername(updateStudentForm.getUsername());
        if (accountByUsername != null){
          throw new BadRequestException("username already exists", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
        }
        currentAccount.setUsername(updateStudentForm.getUsername());
      }
    }

    if (StringUtils.isNotBlank(updateStudentForm.getAvatarPath())) {
      if (!updateStudentForm.getAvatarPath().equals(currentAccount.getAvatarPath())){
        userBaseApiService.deleteFile(currentAccount.getAvatarPath());
      }
    }

    accountMapper.fromUpdateProfileStudentFormToEntity(updateStudentForm, currentAccount);
    studentMapper.fromUpdateProfileStudentFormToEntity(updateStudentForm, currentUser);
    currentUser.setAccount(currentAccount);
    accountRepository.save(currentAccount);
    studentRepository.save(currentUser);
    apiMessageDto.setMessage("Update profile student success");
    return apiMessageDto;
  }

  @PostMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<String> verifyAccountStudent(@RequestBody @Valid VerifyUserForm verifyUserForm){
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

    Student student = studentRepository.findByAccountId(id).orElseThrow(()
    -> new NotFoundException("Student not found", ErrorCode.USER_ERROR_NOT_FOUND));
    student.setStatus(UserBaseConstant.STATUS_ACTIVE);
    if (!UserBaseConstant.STATUS_ACTIVE.equals(account.getStatus())){
      student.setStatus(UserBaseConstant.STATUS_LOCK);
    }
    studentRepository.save(student);
    apiMessageDto.setMessage("verify account student success");
    return apiMessageDto;
  }
}
