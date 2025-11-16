package com.base.auth.controller;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.studentTaskQuestionProgress.StudentTaskQuestionProgressDisplayDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.studentTaskQuestionProgress.CreateStudentTaskQuestionProgressForm;
import com.base.auth.mapper.StudentTaskQuestionProgressMapper;
import com.base.auth.model.Account;
import com.base.auth.model.StudentSubTaskProgress;
import com.base.auth.model.StudentTaskQuestionProgress;
import com.base.auth.model.TaskQuestion;
import com.base.auth.model.criteria.StudentTaskQuestionProgressCriteria;
import com.base.auth.repository.AccountRepository;
import com.base.auth.repository.StudentSubTaskProgressRepository;
import com.base.auth.repository.StudentTaskQuestionProgressRepository;
import com.base.auth.repository.TaskQuestionRepository;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/task-question-progress")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class StudentTaskQuestionProgressController extends ABasicController{
  @Autowired
  StudentTaskQuestionProgressRepository studentTaskQuestionProgressRepository;

  @Autowired
  StudentSubTaskProgressRepository studentSubTaskProgressRepository;

  @Autowired
  TaskQuestionRepository taskQuestionRepository;

  @Autowired
  StudentTaskQuestionProgressMapper studentTaskQuestionProgressMapper;

  @Autowired
  AccountRepository accountRepository;

  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STTQ_C')")
  public ApiMessageDto<String> create(@Valid @RequestBody CreateStudentTaskQuestionProgressForm createStudentTaskQuestionProgressForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not an student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    StudentSubTaskProgress studentSubTaskProgress = studentSubTaskProgressRepository.findById(
        createStudentTaskQuestionProgressForm.getStudentSubTaskProgressId()).orElseThrow(()
    -> new NotFoundException("Student subtask progress not found", ErrorCode.STUDENT_SUBTASK_PROGRESS_ERROR_NOT_FOUND));
    if (Objects.equals(studentSubTaskProgress.getStatus(), ITDreamConstant.STATUS_LOCK)){
      throw new BadRequestException("Task fail. Please reset task", ErrorCode.TASK_ERROR_FAIL);
    }
    TaskQuestion taskQuestion = taskQuestionRepository.findById(
        createStudentTaskQuestionProgressForm.getTaskQuestionId()).orElseThrow(()
    -> new NotFoundException("Task question not found", ErrorCode.TASK_QUESTION_ERROR_NOT_FOUND));
    if (!Objects.equals(taskQuestion.getTask().getId(), studentSubTaskProgress.getTask().getId())){
      throw new BadRequestException("Student task question progress cannot be created", ErrorCode.STUDENT_TASK_QUESTION_PROGRESS_ERROR_NOT_CREATE);
    }
    boolean existStudentTaskQuestionProgress = studentTaskQuestionProgressRepository.existsByTaskQuestionIdAndStudentSubTaskProgressIdAndIsCorrect(taskQuestion.getId(), studentSubTaskProgress.getId(), true);
    if (existStudentTaskQuestionProgress){
      throw new BadRequestException("Student task question progress already exist", ErrorCode.STUDENT_TASK_QUESTION_PROGRESS_ERROR_EXIST);
    }
    StudentTaskQuestionProgress studentTaskQuestionProgress = studentTaskQuestionProgressMapper.fromCreateStudentTaskQuestionProgressFormToEntity(createStudentTaskQuestionProgressForm);
    studentTaskQuestionProgress.setStudentSubTaskProgress(studentSubTaskProgress);
    studentTaskQuestionProgress.setTaskQuestion(taskQuestion);
    if (Objects.equals(taskQuestion.getQuestionType(), ITDreamConstant.QUESTION_TYPE_FILE) || Objects.equals(taskQuestion.getQuestionType(), ITDreamConstant.QUESTION_TYPE_TEXT)){
      studentTaskQuestionProgress.setIsCorrect(true);
    } else {
      if (!createStudentTaskQuestionProgressForm.getIsCorrect()){
        if (studentSubTaskProgress.getErrorCount() >= studentSubTaskProgress.getTask().getMaxErrors()){
          studentSubTaskProgress.setStatus(ITDreamConstant.STATUS_LOCK);
          studentSubTaskProgressRepository.save(studentSubTaskProgress);
          throw new BadRequestException("Task fail", ErrorCode.TASK_ERROR_FAIL);
        }
        studentSubTaskProgress.setErrorCount(studentSubTaskProgress.getErrorCount() + 1);
      }
      studentTaskQuestionProgress.setIsCorrect(createStudentTaskQuestionProgressForm.getIsCorrect());
    }
    studentTaskQuestionProgressRepository.save(studentTaskQuestionProgress);
    apiMessageDto.setMessage("Create student task question progress success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STTQ_ST_L')")
  public ApiMessageDto<ResponseListDto<List<StudentTaskQuestionProgressDisplayDto>>> getListForStudent(
      StudentTaskQuestionProgressCriteria studentTaskQuestionProgressCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<StudentTaskQuestionProgressDisplayDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<StudentTaskQuestionProgressDisplayDto>> responseListDto = new ResponseListDto<>();
    studentTaskQuestionProgressCriteria.setStudentId(getCurrentUser());
    studentTaskQuestionProgressCriteria.setIsCorrect(true);
    Page<StudentTaskQuestionProgress> studentTaskQuestionProgresses = studentTaskQuestionProgressRepository.findAll(studentTaskQuestionProgressCriteria.getSpecification(), pageable);
    responseListDto.setContent(studentTaskQuestionProgressMapper.fromEntityToStudentTaskQuestionProgressDisplayDtoList(studentTaskQuestionProgresses.getContent()));
    responseListDto.setTotalElements(studentTaskQuestionProgresses.getTotalElements());
    responseListDto.setTotalPages(studentTaskQuestionProgresses.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list task question progress success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STTQ_D')")
  public ApiMessageDto<String> delete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    StudentTaskQuestionProgress studentTaskQuestionProgress = studentTaskQuestionProgressRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Student task question progress not found", ErrorCode.STUDENT_TASK_QUESTION_PROGRESS_ERROR_NOT_FOUND));
    studentTaskQuestionProgressRepository.delete(studentTaskQuestionProgress);
    apiMessageDto.setMessage("Delete student task question progress success");
    return apiMessageDto;
  }

  @GetMapping(value = "/answer-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STTQ_ED_LR')")
  public ApiMessageDto<ResponseListDto<List<StudentTaskQuestionProgressDisplayDto>>> getListAnswerByStudent(
      @RequestParam("simulationId") Long simulationId,
      @RequestParam("username") String username,
      Pageable pageable){
    ApiMessageDto<ResponseListDto<List<StudentTaskQuestionProgressDisplayDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<StudentTaskQuestionProgressDisplayDto>> responseListDto = new ResponseListDto<>();
    Account student = accountRepository.findAccountByUsername(username);
    Page<StudentTaskQuestionProgress> taskQuestionProgresses = studentTaskQuestionProgressRepository.findAllByStudentIdAndSimulationId(student.getId(), simulationId, pageable);
    List<StudentTaskQuestionProgressDisplayDto> taskQuestionProgressDisplayDtos = studentTaskQuestionProgressMapper.fromEntityToStudentTaskQuestionProgressDisplayDtoList(taskQuestionProgresses.getContent());
    responseListDto.setContent(taskQuestionProgressDisplayDtos);
    responseListDto.setTotalElements(taskQuestionProgresses.getTotalElements());
    responseListDto.setTotalPages(taskQuestionProgresses.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("Get list answer success");
    return apiMessageDto;
  }
}
