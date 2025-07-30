package com.base.auth.controller;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.studentSubTaskProgress.StudentSubTaskProgressDisplayDto;
import com.base.auth.dto.studentSubTaskProgress.StudentSubTaskProgressDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.studentSubTaskProgress.CreateStudentSubTaskProgressForm;
import com.base.auth.form.studentSubTaskProgress.RequestStudentSubTaskProgressForm;
import com.base.auth.mapper.StudentSubTaskProgressMapper;
import com.base.auth.model.Student;
import com.base.auth.model.StudentSubTaskProgress;
import com.base.auth.model.StudentTaskQuestionProgress;
import com.base.auth.model.SubTask;
import com.base.auth.model.criteria.StudentSubTaskProgressCriteria;
import com.base.auth.repository.StudentRepository;
import com.base.auth.repository.StudentSubTaskProgressRepository;
import com.base.auth.repository.StudentTaskQuestionProgressRepository;
import com.base.auth.repository.SubTaskRepository;
import java.util.List;
import java.util.Objects;
import javax.transaction.Transactional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/subtask-progress")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class StudentSubTaskProgressController extends ABasicController{
  @Autowired
  StudentSubTaskProgressRepository studentSubTaskProgressRepository;

  @Autowired
  StudentSubTaskProgressMapper studentSubTaskProgressMapper;

  @Autowired
  SubTaskRepository subTaskRepository;

  @Autowired
  StudentRepository studentRepository;

  @Autowired
  StudentTaskQuestionProgressRepository studentTaskQuestionProgressRepository;

  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STSP_C')")
  public ApiMessageDto<String> create(@Valid @RequestBody CreateStudentSubTaskProgressForm createStudentSubTaskProgressForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Student student = studentRepository.findById(getCurrentUser()).orElseThrow(()
    -> new NotFoundException("Student not found", ErrorCode.USER_ERROR_NOT_FOUND));
    if (!isStudent()){
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    SubTask subTask = subTaskRepository.findById(createStudentSubTaskProgressForm.getSubTaskId()).orElseThrow(()
    -> new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    StudentSubTaskProgress existStudentSubTaskProgress = studentSubTaskProgressRepository.findBySubTaskIdAndStudentId(
        subTask.getId(), getCurrentUser()).orElse(null);
    if (existStudentSubTaskProgress != null){
      throw new BadRequestException("Student subtask progress already exist", ErrorCode.STUDENT_SUBTASK_PROGRESS_ERROR_EXIST);
    }
    StudentSubTaskProgress studentSubTaskProgress = new StudentSubTaskProgress();
    studentSubTaskProgress.setStudent(student);
    studentSubTaskProgress.setSubTask(subTask);
    studentSubTaskProgress.setState(UserBaseConstant.STATE_IN_PROGRESS);
    studentSubTaskProgressRepository.save(studentSubTaskProgress);
    apiMessageDto.setMessage("Create success");
    return apiMessageDto;
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STSP_L')")
  public ApiMessageDto<ResponseListDto<List<StudentSubTaskProgressDto>>> getList(
      StudentSubTaskProgressCriteria studentSubTaskProgressCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<StudentSubTaskProgressDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<StudentSubTaskProgressDto>> responseListDto = new ResponseListDto<>();
    Page<StudentSubTaskProgress> studentSubTaskProgresses = studentSubTaskProgressRepository.findAll(studentSubTaskProgressCriteria.getSpecification(), pageable);
    responseListDto.setContent(studentSubTaskProgressMapper.fromEntityToStudentSubTaskProgressDtoList(studentSubTaskProgresses.getContent()));
    responseListDto.setTotalElements(studentSubTaskProgresses.getTotalElements());
    responseListDto.setTotalPages(studentSubTaskProgresses.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STSP_ST_L')")
  public ApiMessageDto<ResponseListDto<List<StudentSubTaskProgressDisplayDto>>> getListForStudent(
      StudentSubTaskProgressCriteria studentSubTaskProgressCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<StudentSubTaskProgressDisplayDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<StudentSubTaskProgressDisplayDto>> responseListDto = new ResponseListDto<>();
    studentSubTaskProgressCriteria.setStudentId(getCurrentUser());
    Page<StudentSubTaskProgress> studentSubTaskProgresses = studentSubTaskProgressRepository.findAll(studentSubTaskProgressCriteria.getSpecification(), pageable);
    responseListDto.setContent(studentSubTaskProgressMapper.fromEntityToStudentSubTaskProgressDtoDisplayList(studentSubTaskProgresses.getContent()));
    responseListDto.setTotalElements(studentSubTaskProgresses.getTotalElements());
    responseListDto.setTotalPages(studentSubTaskProgresses.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list success");
    return apiMessageDto;
  }

  @PutMapping(value = "/complete", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STSP_CPL')")
  @Transactional
  public ApiMessageDto<String> complete(@Valid @RequestBody RequestStudentSubTaskProgressForm requestStudentSubTaskProgressForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not an student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    SubTask subTask = subTaskRepository.findById(requestStudentSubTaskProgressForm.getSubTaskId()).orElseThrow(()
    -> new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    StudentSubTaskProgress studentSubTaskProgress = studentSubTaskProgressRepository.findBySubTaskIdAndStudentId(subTask.getId(), getCurrentUser()).orElseThrow(()
    -> new NotFoundException("Student subtask progress not found", ErrorCode.STUDENT_SUBTASK_PROGRESS_ERROR_NOT_FOUND));
    studentSubTaskProgress.setState(UserBaseConstant.STATE_COMPLETED);
    studentSubTaskProgress.setErrorCount(UserBaseConstant.RESTART_ERROR_COUNT);
    StudentTaskQuestionProgress studentTaskQuestionProgress = studentTaskQuestionProgressRepository.findFirstByStudentSubTaskProgressId(studentSubTaskProgress.getId()).orElse(null);
    if (studentTaskQuestionProgress != null && Objects.equals(studentTaskQuestionProgress.getTaskQuestion().getQuestionType(), UserBaseConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
      studentTaskQuestionProgressRepository.deleteAllByStudentSubTaskProgressId(studentSubTaskProgress.getId());
    }
    studentSubTaskProgressRepository.save(studentSubTaskProgress);
    apiMessageDto.setMessage("Complete student subtask progress");
    return apiMessageDto;
  }

  @PutMapping(value = "/restart", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STSP_RES')")
  @Transactional
  public ApiMessageDto<String> restart(@Valid @RequestBody RequestStudentSubTaskProgressForm requestStudentSubTaskProgressForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not an student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    SubTask subTask = subTaskRepository.findById(requestStudentSubTaskProgressForm.getSubTaskId()).orElseThrow(()
        -> new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    StudentSubTaskProgress studentSubTaskProgress = studentSubTaskProgressRepository.findBySubTaskIdAndStudentId(subTask.getId(), getCurrentUser()).orElseThrow(()
        -> new NotFoundException("Student subtask progress not found", ErrorCode.STUDENT_SUBTASK_PROGRESS_ERROR_NOT_FOUND));
    studentSubTaskProgress.setCurrentAttempt(studentSubTaskProgress.getCurrentAttempt() + 1);
    studentSubTaskProgress.setErrorCount(UserBaseConstant.RESTART_ERROR_COUNT);
    studentTaskQuestionProgressRepository.deleteAllByStudentSubTaskProgressId(studentSubTaskProgress.getId());
    apiMessageDto.setMessage("Restart student subtask progress success");
    return apiMessageDto;
  }

  @PutMapping(value = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STSP_ACC')")
  public ApiMessageDto<String> access(@Valid @RequestBody RequestStudentSubTaskProgressForm request, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()) {
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    SubTask subTask = subTaskRepository.findById(request.getSubTaskId()).orElseThrow(() ->
        new NotFoundException("Subtask not found", ErrorCode.SUBTASK_ERROR_NOT_FOUND));
    StudentSubTaskProgress progress = studentSubTaskProgressRepository.findBySubTaskIdAndStudentId(subTask.getId(), getCurrentUser()).orElseThrow(() ->
        new NotFoundException("Student subtask progress not found", ErrorCode.STUDENT_SUBTASK_PROGRESS_ERROR_NOT_FOUND));
    progress.setCurrentAttempt(progress.getCurrentAttempt() + 1);
    studentSubTaskProgressRepository.save(progress);
    apiMessageDto.setMessage("Access student subtask progress success");
    return apiMessageDto;
  }
}
