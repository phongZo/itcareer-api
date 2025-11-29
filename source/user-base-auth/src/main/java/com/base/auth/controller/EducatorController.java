package com.base.auth.controller;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.account.OtpDto;
import com.base.auth.dto.educator.EducatorDto;
import com.base.auth.dto.educator.ProfileEducatorDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.exception.UnauthorizationException;
import com.base.auth.form.account.VerifyUserForm;
import com.base.auth.form.educator.RequestEducatorIdForm;
import com.base.auth.form.educator.SignUpEducatorForm;
import com.base.auth.form.educator.UpdateEducatorForm;
import com.base.auth.form.educator.UpdateProfileEducatorForm;
import com.base.auth.mapper.AccountMapper;
import com.base.auth.mapper.EducatorMapper;
import com.base.auth.model.Account;
import com.base.auth.model.Educator;
import com.base.auth.model.Group;
import com.base.auth.model.Simulation;
import com.base.auth.model.Task;
import com.base.auth.model.criteria.EducatorCriteria;
import com.base.auth.repository.AccountRepository;
import com.base.auth.repository.AchievementRepository;
import com.base.auth.repository.EducatorRepository;
import com.base.auth.repository.GroupRepository;
import com.base.auth.repository.ReviewRepository;
import com.base.auth.repository.ReviewSubmissionRepository;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.StudentSubTaskProgressRepository;
import com.base.auth.repository.StudentTaskQuestionProgressRepository;
import com.base.auth.repository.TaskQuestionRepository;
import com.base.auth.repository.TaskRepository;
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
@RequestMapping("/v1/educator")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class EducatorController extends ABasicController{
  @Autowired
  EducatorRepository educatorRepository;

  @Autowired
  AccountRepository accountRepository;

  @Autowired
  EducatorMapper educatorMapper;

  @Autowired
  AccountMapper accountMapper;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  GroupRepository groupRepository;

  @Autowired
  SimulationRepository simulationRepository;

  @Autowired
  TaskRepository taskRepository;

  @Autowired
  TaskQuestionRepository taskQuestionRepository;

  @Autowired
  StudentSubTaskProgressRepository studentSubTaskProgressRepository;

  @Autowired
  StudentTaskQuestionProgressRepository studentTaskQuestionProgressRepository;

  @Autowired
  AchievementRepository achievementRepository;

  @Autowired
  ReviewRepository reviewRepository;

  @Autowired
  ReviewSubmissionRepository reviewSubmissionRepository;

  @PostMapping(value = "/signup", produces= MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<OtpDto> create(@Valid @RequestBody SignUpEducatorForm signUpEducatorForm, BindingResult bindingResult)
  {
    ApiMessageDto<OtpDto> apiMessageDto = new ApiMessageDto<>();
    Boolean existUsername = accountRepository.existsByUsername(signUpEducatorForm.getUsername());
    if (existUsername){
      throw new BadRequestException("Username already exists", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
    }

    Boolean existEmail = accountRepository.existsByEmail(signUpEducatorForm.getEmail());
    if (existEmail)
    {
      throw new BadRequestException("Email already exists", ErrorCode.ACCOUNT_ERROR_EMAIL_EXIST);
    }

    Boolean existPhone = accountRepository.existsByPhone(signUpEducatorForm.getPhone());
    if (existPhone)
    {
      throw new BadRequestException("Phone already exists", ErrorCode.ACCOUNT_ERROR_PHONE_EXIST);
    }
    Account account = accountMapper.fromSignUpEducatorToAccount(signUpEducatorForm);
    account.setPassword(passwordEncoder.encode(signUpEducatorForm.getPassword()));
    account.setKind(ITDreamConstant.USER_KIND_EDUCATOR);
    Group group = groupRepository.findFirstByKind(ITDreamConstant.USER_KIND_EDUCATOR);
    if (group == null){
      throw new NotFoundException("Group not found", ErrorCode.GROUP_ERROR_NOT_FOUND);
    }
    account.setGroup(group);
    account.setStatus(ITDreamConstant.STATUS_PENDING);
    String otp = userBaseApiService.getRequestOTP();
    account.setAttemptCode(0);
    account.setResetPwdCode(otp);
    account.setResetPwdTime(new Date());
    accountRepository.save(account);

    Educator educator = new Educator();
    educator.setAccount(account);
    educatorRepository.save(educator);

    sendVerifyAccount(account);
    OtpDto otpDto = new OtpDto();
    String hash = AESUtils.encrypt (account.getId()+";"+otp, true);
    otpDto.setIdHash(hash);
    apiMessageDto.setData(otpDto);
    apiMessageDto.setMessage("Sign up success, please check email.");
    return apiMessageDto;
  }

  @GetMapping(value = "/list", produces= MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ED_L')")
  public ApiMessageDto<ResponseListDto<List<EducatorDto>>> getListEducator(EducatorCriteria educatorCriteria , Pageable pageable)
  {
    ApiMessageDto<ResponseListDto<List<EducatorDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<EducatorDto>> responseListDto = new ResponseListDto<>();
    Page<Educator> listEducator = educatorRepository.findAll(educatorCriteria.getSpecification(),pageable);
    responseListDto.setContent(educatorMapper.fromEducatorListToEducatorDtoList(listEducator.getContent()));
    responseListDto.setTotalElements(listEducator.getTotalElements());
    responseListDto.setTotalPages(listEducator.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list educator success");
    return apiMessageDto;
  }

  @GetMapping(value = "/get/{id}", produces= MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ED_V')")
  public ApiMessageDto<EducatorDto> getEducator(@PathVariable("id") Long id)
  {
    if (!isAdmin()){
      throw new UnauthorizationException("User is not an admin");
    }
    ApiMessageDto<EducatorDto> apiMessageDto = new ApiMessageDto<>();
    Educator educator = educatorRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Educator not found", ErrorCode.USER_ERROR_NOT_FOUND));
    apiMessageDto.setData(educatorMapper.fromEntityToEducatorDto(educator));
    apiMessageDto.setMessage("Get educator success");
    return apiMessageDto;
  }

  @Transactional
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ED_U')")
  public ApiMessageDto<String> updateEducator(@Valid @RequestBody UpdateEducatorForm updateEducatorForm, BindingResult bindingResult) {
    if (!isAdmin()){
      throw new UnauthorizationException("User is not an admin");
    }
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Educator educator = educatorRepository.findById(updateEducatorForm.getId()).orElseThrow(()
    -> new NotFoundException("Educator not found", ErrorCode.USER_ERROR_NOT_FOUND));

    Account account = accountRepository.findById(educator.getAccount().getId()).orElseThrow(()
    -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

    if (!Objects.equals(educator.getAccount().getUsername(), updateEducatorForm.getUsername())){
      Boolean existUsername = accountRepository.existsByUsername(updateEducatorForm.getUsername());
      if (existUsername){
        throw new BadRequestException("Username already exist", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
      }
      account.setUsername(updateEducatorForm.getUsername());
    }

    if (!Objects.equals(educator.getAccount().getEmail(), updateEducatorForm.getEmail()))
    {
      Boolean existEmail = accountRepository.existsByEmail(updateEducatorForm.getEmail());
      if(existEmail)
      {
        throw new BadRequestException("email already exist", ErrorCode.ACCOUNT_ERROR_EMAIL_EXIST);
      }
      account.setEmail(updateEducatorForm.getEmail());
    }

    if (!Objects.equals(educator.getAccount().getPhone(), updateEducatorForm.getPhone()))
    {
      Boolean existPhone = accountRepository.existsByPhone(updateEducatorForm.getPhone());
      if(existPhone)
      {
        throw new BadRequestException("phone already exist", ErrorCode.ACCOUNT_ERROR_PHONE_EXIST);
      }
      account.setPhone(updateEducatorForm.getPhone());
    }

    if(StringUtils.isNoneBlank(updateEducatorForm.getPassword()))
    {
      account.setPassword(passwordEncoder.encode(updateEducatorForm.getPassword()));
    }

    if (StringUtils.isNotBlank(updateEducatorForm.getAvatarPath())) {
      if (!updateEducatorForm.getAvatarPath().equals(account.getAvatarPath())){
        userBaseApiService.deleteByFilePath(account.getAvatarPath());
        account.setAvatarPath(updateEducatorForm.getAvatarPath());
      }
    }
    accountMapper.fromUpdateEducatorFormToEntity(updateEducatorForm, account);
    accountRepository.save(account);
    apiMessageDto.setMessage("Update educator success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/delete/{id}")
  @PreAuthorize("hasRole('ED_D')")
  @Transactional
  public ApiMessageDto<String> deleteEducator(@PathVariable("id") Long id)
  {
    if (!isAdmin()){
      throw new UnauthorizationException("User is not an admin");
    }
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Educator educator = educatorRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Educator not found", ErrorCode.USER_ERROR_NOT_FOUND));

    Account account = educator.getAccount();
    if (account == null){
      throw new BadRequestException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    if (Objects.equals(account.getKind(), ITDreamConstant.USER_KIND_ADMIN)){
      throw new BadRequestException("Not allow delete admin", ErrorCode.ACCOUNT_ERROR_NOT_ALLOW_DELETE_ADMIN);
    }

    List<Task> tasks = taskRepository.findAllByEducatorId(id);
    for (Task task : tasks){
      deleteTaskFiles(task);
    }

    List<Simulation> simulations = simulationRepository.findAllByEducatorId(id);
    for (Simulation simulation : simulations){
      deleteSimulationFiles(simulation);
      achievementRepository.setNullSimulationId(simulation.getId());
    }

    userBaseApiService.deleteByFilePath(educator.getAccount().getAvatarPath());
    studentTaskQuestionProgressRepository.deleteAllByEducatorId(id);
    studentSubTaskProgressRepository.deleteAllByEducatorId(id);
    reviewSubmissionRepository.deleteAllByEducatorId(id);
    reviewRepository.deleteAllByEducatorId(id);
    taskQuestionRepository.deleteAllByEducatorId(id);
    taskRepository.deleteAllSubTaskByEducatorId(id);
    taskRepository.deleteAllTaskByEducatorId(id);
    simulationRepository.deleteAllByEducatorId(id);
    educatorRepository.delete(educator);
    accountRepository.delete(account);
    apiMessageDto.setMessage("Delete educator success");
    return apiMessageDto;
  }

  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ED_U_P')")
  public ApiMessageDto<ProfileEducatorDto> getProfileForEducator(){
    ApiMessageDto<ProfileEducatorDto> apiMessageDto = new ApiMessageDto<>();
    Account account = accountRepository.findById(getCurrentUser()).orElseThrow(
        () -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
    Educator educator = educatorRepository.findById(account.getId()).orElseThrow(
        () -> new NotFoundException("Educator not found", ErrorCode.USER_ERROR_NOT_FOUND));
    ProfileEducatorDto educatorDto = educatorMapper.fromEducatorToProfileDto(educator);
    apiMessageDto.setData(educatorDto);
    apiMessageDto.setMessage("Get profile educator success");
    return apiMessageDto;
  }

  @PutMapping(value = "/client_update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ED_U_U')")
  public ApiMessageDto<String> updateProfileForEducator(@Valid @RequestBody UpdateProfileEducatorForm updateEducatorForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Account currentAccount = accountRepository.findById(getCurrentUser()).orElseThrow(() ->
        new NotFoundException("account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
    Educator currentUser = educatorRepository.findById(currentAccount.getId()).orElseThrow(() ->
        new NotFoundException("educator not found", ErrorCode.USER_ERROR_NOT_FOUND));

    if (!Objects.equals(currentAccount.getUsername(), updateEducatorForm.getUsername())){
      Boolean existUsername = accountRepository.existsByUsername(updateEducatorForm.getUsername());
      if (existUsername){
        throw new BadRequestException("Username already exist", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
      }
      currentAccount.setUsername(updateEducatorForm.getUsername());
    }

    if (!Objects.equals(currentAccount.getPhone(), updateEducatorForm.getPhone())){
      Boolean existPhone = accountRepository.existsByPhone(updateEducatorForm.getPhone());
      if (existPhone){
        throw new BadRequestException("Phone already exist", ErrorCode.ACCOUNT_ERROR_PHONE_EXIST);
      }
      currentAccount.setPhone(updateEducatorForm.getPhone());
    }

    if (StringUtils.isNotBlank(updateEducatorForm.getAvatarPath())) {
      if (!updateEducatorForm.getAvatarPath().equals(currentAccount.getAvatarPath())){
        userBaseApiService.deleteByFilePath(currentAccount.getAvatarPath());
        currentAccount.setAvatarPath(updateEducatorForm.getAvatarPath());
      }
    }

    accountMapper.fromUpdateProfileEducatorFormToEntity(updateEducatorForm, currentAccount);
    currentUser.setAccount(currentAccount);
    accountRepository.save(currentAccount);
    educatorRepository.save(currentUser);
    apiMessageDto.setMessage("Update profile educator success");
    return apiMessageDto;
  }

  @PostMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<String> verifyAccountEducator(@RequestBody @Valid VerifyUserForm verifyUserForm){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    String[] hash = AESUtils.decrypt(verifyUserForm.getIdHash(),true).split(";",2);
    Long id = ConvertUtils.convertStringToLong(hash[0]);
    if(id <= 0){
      throw new BadRequestException("Incorrect hash verification", ErrorCode.ACCOUNT_ERROR_INCORRECT_HASH_VERIFICATION);
    }

    Account account = accountRepository.findById(id).orElse(null);
    if (account == null ) {
      throw new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }

    if (!Objects.equals(ITDreamConstant.STATUS_PENDING, account.getStatus())){
      throw new BadRequestException("Educator cannot be verified", ErrorCode.USER_ERROR_VERIFY_FAILED);
    }

    if(account.getAttemptCode() >= ITDreamConstant.MAX_ATTEMPT_FORGET_PWD){
      account.setStatus(ITDreamConstant.STATUS_LOCK);
      accountRepository.save(account);
      throw new BadRequestException("Account has been locked", ErrorCode.ACCOUNT_ERROR_LOCKED);
    }

    if(!account.getResetPwdCode().equals(verifyUserForm.getOtp()) ||
        (new Date().getTime() - account.getResetPwdTime().getTime() >= ITDreamConstant.MAX_TIME_FORGET_PWD)){

      //tang so lan
      account.setAttemptCode(account.getAttemptCode()+1);
      accountRepository.save(account);
      throw new BadRequestException("OTP invalid", ErrorCode.ACCOUNT_ERROR_OPT_INVALID);
    }

    account.setResetPwdTime(null);
    account.setResetPwdCode(null);
    account.setAttemptCode(null);
    account.setStatus(ITDreamConstant.STATUS_WAITING_APPROVE);
    accountRepository.save(account);
    apiMessageDto.setMessage("verify account educator success. Please wait for approval");
    return apiMessageDto;
  }

  @PutMapping(value = "/approve", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ED_AP')")
  public ApiMessageDto<String> approveAccountEducator(@Valid @RequestBody RequestEducatorIdForm requestEducatorIdForm, BindingResult bindingResult){
    if (!isAdmin()){
      throw new UnauthorizationException("User is not an admin");
    }
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Educator educator = educatorRepository.findById(requestEducatorIdForm.getId()).orElseThrow(()
        -> new NotFoundException("Educator not found", ErrorCode.USER_ERROR_NOT_FOUND));

    Account account = accountRepository.findById(educator.getAccount().getId()).orElseThrow(()
        -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

    if (!Objects.equals(ITDreamConstant.STATUS_WAITING_APPROVE, account.getStatus())){
      throw new BadRequestException("Educator cannot be approved", ErrorCode.USER_ERROR_NOT_APPROVE);
    }
    account.setStatus(ITDreamConstant.STATUS_ACTIVE);
    accountRepository.save(account);
    apiMessageDto.setMessage("Approve educator success");
    return apiMessageDto;
  }

  @PutMapping(value = "/reject", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ED_RJ')")
  public ApiMessageDto<String> rejectAccountEducator(@Valid @RequestBody RequestEducatorIdForm requestEducatorIdForm, BindingResult bindingResult){
    if (!isAdmin()){
      throw new UnauthorizationException("User is not an admin");
    }
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Educator educator = educatorRepository.findById(requestEducatorIdForm.getId()).orElseThrow(()
        -> new NotFoundException("Educator not found", ErrorCode.USER_ERROR_NOT_FOUND));

    Account account = accountRepository.findById(educator.getAccount().getId()).orElseThrow(()
        -> new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));

    if (!Objects.equals(ITDreamConstant.STATUS_WAITING_APPROVE, account.getStatus())){
      throw new BadRequestException("Educator cannot be rejected", ErrorCode.USER_ERROR_NOT_REJECT);
    }
    account.setStatus(ITDreamConstant.STATUS_REJECT);
    accountRepository.save(account);
    apiMessageDto.setMessage("Reject educator success");
    return apiMessageDto;
  }

  private void deleteSimulationFiles(Simulation simulation) {
    userBaseApiService.deleteByFilePath(simulation.getImagePath());
    userBaseApiService.deleteByFilePath(simulation.getVideoPath());
  }

  private void deleteTaskFiles(Task task) {
    userBaseApiService.deleteByFilePath(task.getImagePath());
    userBaseApiService.deleteByFilePath(task.getFilePath());
    userBaseApiService.deleteByFilePath(task.getVideoPath());
  }
}
