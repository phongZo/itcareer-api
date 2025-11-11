package com.base.auth.controller;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.account.OtpDto;
import com.base.auth.dto.account.ProfileAccountDto;
import com.base.auth.dto.reviewSubmission.ReviewedStudentProjection;
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
import com.base.auth.model.Review;
import com.base.auth.model.Simulation;
import com.base.auth.model.Student;
import com.base.auth.model.criteria.StudentCriteria;
import com.base.auth.repository.AccountRepository;
import com.base.auth.repository.GroupRepository;
import com.base.auth.repository.ReviewRepository;
import com.base.auth.repository.ReviewSubmissionRepository;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.StudentRepository;
import com.base.auth.repository.StudentSubTaskProgressRepository;
import com.base.auth.repository.StudentTaskQuestionProgressRepository;
import com.base.auth.repository.TaskRepository;
import com.base.auth.utils.AESUtils;
import com.base.auth.utils.ConvertUtils;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.RequestParam;
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
  private StudentSubTaskProgressRepository studentSubTaskProgressRepository;

  @Autowired
  private StudentTaskQuestionProgressRepository studentTaskQuestionProgressRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  SimulationRepository simulationRepository;

  @Autowired
  TaskRepository taskRepository;

  @Autowired
  ReviewSubmissionRepository reviewSubmissionRepository;

  @PostMapping(value = "/signup", produces= MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<OtpDto> create(@Valid @RequestBody SignUpStudentForm signUpStudentForm, BindingResult bindingResult)
  {
    ApiMessageDto<OtpDto> apiMessageDto = new ApiMessageDto<>();

    Account accountByUsername = accountRepository.findAccountByUsername(signUpStudentForm.getUsername());
    if (accountByUsername != null){
      throw new BadRequestException("username already exists", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
    }

    Account accountByEmail = accountRepository.findAccountByEmail(signUpStudentForm.getEmail());
    if (accountByEmail!=null)
    {
      throw new BadRequestException("email already exists", ErrorCode.ACCOUNT_ERROR_EMAIL_EXIST);
    }

    Account accountByPhone = accountRepository.findAccountByPhone(signUpStudentForm.getPhone());
    if (accountByPhone!=null)
    {
      throw new BadRequestException("phone already exists", ErrorCode.ACCOUNT_ERROR_PHONE_EXIST);
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
        userBaseApiService.deleteByFilePath(account.getAvatarPath());
      }
      account.setAvatarPath(updateStudentForm.getAvatarPath());
    }

    accountMapper.fromUpdateStudentFormToEntity(updateStudentForm, account);
    accountRepository.save(account);
    apiMessageDto.setMessage("update success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/delete/{id}")
  @PreAuthorize("hasRole('ST_D')")
  @Transactional
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

    userBaseApiService.deleteByFilePath(student.getAccount().getAvatarPath());
    studentTaskQuestionProgressRepository.deleteAllByStudentId(id);
    studentSubTaskProgressRepository.deleteAllByStudentId(id);

    List<Review> reviews = reviewRepository.findAllByStudentId(id);
    for (Review review : reviews) {
      Simulation simulation = review.getSimulation();
      int totalReviewer = reviewRepository.countBySimulationId(simulation.getId());
      reviewRepository.delete(review);

      if (totalReviewer > 1) {
        float avgRating = ((simulation.getAvgRating() * totalReviewer) - review.getStar()) / (totalReviewer - 1);
        simulation.setAvgRating(avgRating);
      } else {
        simulation.setAvgRating(0F);
      }
      simulationRepository.save(simulation);
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
    Student student = studentRepository.findById(account.getId()).orElseThrow(
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
    Student currentUser = studentRepository.findById(currentAccount.getId()).orElseThrow(() ->
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
        userBaseApiService.deleteByFilePath(currentAccount.getAvatarPath());
      }
      currentAccount.setAvatarPath(updateStudentForm.getAvatarPath());
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

    if (!Objects.equals(UserBaseConstant.STATUS_PENDING, account.getStatus())){
      throw new BadRequestException("student cannot be verified", ErrorCode.USER_ERROR_VERIFY_FAILED);
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
    apiMessageDto.setMessage("verify account student success");
    return apiMessageDto;
  }

  @GetMapping(value = "/complete-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ST_ED_CL')")
  public ApiMessageDto<ResponseListDto<List<ProfileStudentDto>>> getListStudentComplete(@RequestParam("simulationId") Long simulationId, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<ProfileStudentDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<ProfileStudentDto>> responseListDto = new ResponseListDto<>();
    Long totalTasks = taskRepository.countBySimulationId(simulationId);
    Page<Student> students = studentRepository.findStudentsCompletedSimulation(simulationId, UserBaseConstant.STATE_STUDENT_SUBTASK_PROGRESS_COMPLETED, totalTasks, pageable);
    List<ProfileStudentDto> studentDtos = studentMapper.fromStudentToProfileDtoList(students.getContent());
    Map<String, Boolean> reviewedMap = createReviewedMapBySimulation(simulationId);
    setIsReviewedByMap(studentDtos, reviewedMap);
    responseListDto.setContent(studentDtos);
    responseListDto.setTotalElements(students.getTotalElements());
    responseListDto.setTotalPages(students.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list student complete simulation success");
    return apiMessageDto;
  }

  // Chuyển nội dung từ DTO sang Map để dễ gán field isReviewed
  private Map<String, Boolean> createReviewedMapBySimulation(Long simulationId){
    List<ReviewedStudentProjection> reviewedList =
        reviewSubmissionRepository.findReviewedStudentUsernamesBySimulationId(simulationId);

    if (reviewedList == null || reviewedList.isEmpty()) {
      return Collections.emptyMap();
    }

    return reviewedList.stream()
        .filter(p -> p.getUsername() != null)
        .collect(Collectors.toMap(
            ReviewedStudentProjection::getUsername,
            ReviewedStudentProjection::getIsReviewed,
            (a, b) -> a // nếu trùng username, giữ giá trị đầu tiên
        ));
  }


  // Gán isReviewed dựa trên username (nếu có trong map => lấy giá trị DB, nếu không => null)
  private void  setIsReviewedByMap(List<ProfileStudentDto> dtos, Map<String, Boolean> reviewedMap){
    for (ProfileStudentDto dto : dtos) {
      ProfileAccountDto acc = dto.getProfileAccountDto();
      if (acc != null) {
        String username = acc.getUsername();
        if (username != null && reviewedMap.containsKey(username)) {
          dto.setIsReviewed(reviewedMap.get(username));
        } else {
          dto.setIsReviewed(null);
        }
      } else {
        dto.setIsReviewed(null);
      }
    }
  }
}
