package com.base.auth.controller;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.reviewSubmission.ReviewSubmissionClientDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.notification.CreateNotificationForm;
import com.base.auth.form.reviewSubmission.CreateReviewSubmissionForm;
import com.base.auth.form.reviewSubmission.UpdateReviewSubmissionForm;
import com.base.auth.mapper.ReviewSubmissionMapper;
import com.base.auth.model.Account;
import com.base.auth.model.ReviewSubmission;
import com.base.auth.model.Simulation;
import com.base.auth.model.Student;
import com.base.auth.repository.AccountRepository;
import com.base.auth.repository.NotificationRepository;
import com.base.auth.repository.ReviewSubmissionRepository;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.StudentSubTaskProgressRepository;
import com.base.auth.repository.TaskRepository;
import com.base.auth.service.NotificationService;
import java.util.Objects;
import javax.transaction.Transactional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/v1/review-submission")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ReviewSubmissionController extends ABasicController{
  @Autowired
  ReviewSubmissionRepository reviewSubmissionRepository;

  @Autowired
  ReviewSubmissionMapper reviewSubmissionMapper;

  @Autowired
  SimulationRepository simulationRepository;

  @Autowired
  AccountRepository accountRepository;

  @Autowired
  TaskRepository taskRepository;

  @Autowired
  StudentSubTaskProgressRepository studentSubTaskProgressRepository;

  @Autowired
  NotificationService notificationService;

  @Autowired
  NotificationRepository notificationRepository;

  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('RESUB_C')")
  ApiMessageDto<String> create(@RequestBody @Valid CreateReviewSubmissionForm request, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    Simulation simulation = simulationRepository.findById(request.getSimulationId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    if (!Objects.equals(simulation.getEducator().getId(), getCurrentUser())){
      throw new BadRequestException("Educator does not own this simulation", ErrorCode.SIMULATION_ERROR_NOT_AUTHORIZED);
    }
    Account account = accountRepository.findAccountByUsername(request.getUsername());
    if (account == null){
      throw new NotFoundException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
    }
    Student student = studentRepository.findById(account.getId()).orElseThrow(()
    -> new NotFoundException("Student not found", ErrorCode.USER_ERROR_NOT_FOUND));
    boolean existReviewSubmission = reviewSubmissionRepository.existsBySimulationIdAndStudentId(simulation.getId(),student.getId());
    if (existReviewSubmission){
      throw new BadRequestException("Review submission already exist", ErrorCode.REVIEW_SUBMISSION_ERROR_EXIST);
    }
    Long totalTasks = taskRepository.countByKindAndSimulationId(ITDreamConstant.TASK_KIND_SUBTASK,simulation.getId());
    Long completedTasks = studentSubTaskProgressRepository.countByStateAndStudentIdAndTaskSimulationId(
        ITDreamConstant.STATE_STUDENT_SUBTASK_PROGRESS_COMPLETED, student.getId(),
        simulation.getId());
    if (totalTasks == 0 || completedTasks != totalTasks){
      throw new BadRequestException("Unable to create a review submission", ErrorCode.REVIEW_SUBMISSION_ERROR_CREATE);
    }
    ReviewSubmission reviewSubmission = reviewSubmissionMapper.fromCreateReviewSubmissionToEntity(request);
    reviewSubmission.setSimulation(simulation);
    reviewSubmission.setStudent(student);
    reviewSubmissionRepository.save(reviewSubmission);
    sendNotification(reviewSubmission.getStudent().getId(), reviewSubmission.getSimulation().getTitle(), reviewSubmission.getId());
    apiMessageDto.setMessage("Create review submission success");
    return apiMessageDto;
  }

  @GetMapping(value = "/educator-get/{simulationId}/student/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('RESUB_ED_V')")
  public ApiMessageDto<ReviewSubmissionClientDto> getForEducator(@PathVariable("simulationId") Long simulationId, @PathVariable("username") String studentName){
    ApiMessageDto<ReviewSubmissionClientDto> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    ReviewSubmission reviewSubmission = reviewSubmissionRepository.findBySimulationIdAndStudentUsername(simulationId, studentName).orElseThrow(()
        -> new NotFoundException("Review submission not found", ErrorCode.REVIEW_SUBMISSION_ERROR_NOT_FOUND));
    apiMessageDto.setData(reviewSubmissionMapper.fromEntityToReviewSubmissionDtoForClient(reviewSubmission));
    apiMessageDto.setMessage("Get review submission success");
    return apiMessageDto;
  }

  @GetMapping(value = "/student-get/{simulationId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('RESUB_ST_V')")
  public ApiMessageDto<ReviewSubmissionClientDto> getForStudent(@PathVariable("simulationId") Long simulationId){
    ApiMessageDto<ReviewSubmissionClientDto> apiMessageDto = new ApiMessageDto<>();
    ReviewSubmission reviewSubmission = reviewSubmissionRepository.findBySimulationIdAndStudentId(simulationId, getCurrentUser()).orElse(null);
    apiMessageDto.setData(reviewSubmissionMapper.fromEntityToReviewSubmissionDtoForClient(reviewSubmission));
    apiMessageDto.setMessage("Get review submission success");
    return apiMessageDto;
  }

  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('RESUB_U')")
  public ApiMessageDto<String> update(@RequestBody @Valid UpdateReviewSubmissionForm request, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    ReviewSubmission reviewSubmission = reviewSubmissionRepository.findById(request.getId()).orElseThrow(()
    -> new NotFoundException("Review submission not found",ErrorCode.REVIEW_SUBMISSION_ERROR_NOT_FOUND));
    reviewSubmissionMapper.fromUpdateReviewSubmissionToEntity(request, reviewSubmission);
    reviewSubmissionRepository.save(reviewSubmission);
    apiMessageDto.setMessage("Update review submission success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('RESUB_D')")
  @Transactional
  public ApiMessageDto<String> delete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isEducator()){
      throw new BadRequestException("User is not an educator", ErrorCode.USER_ERROR_NOT_EDUCATOR);
    }
    ReviewSubmission reviewSubmission = reviewSubmissionRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Review submission not found", ErrorCode.REVIEW_SUBMISSION_ERROR_NOT_FOUND));
    notificationRepository.deleteByReceiverIdAndRefId(reviewSubmission.getStudent().getId(), reviewSubmission.getId());
    reviewSubmissionRepository.delete(reviewSubmission);
    apiMessageDto.setMessage("Delete review submission success");
    return apiMessageDto;
  }

  private void sendNotification(Long receiverId, String simulationTitle, Long refId){
    String title = "Bạn đã được đánh giá";
    String message = String.format("Giảng viên đã gửi đánh giá cho bài mô phỏng: " + simulationTitle);
    CreateNotificationForm notificationForm = new CreateNotificationForm();
    notificationForm.setUserId(receiverId);
    notificationForm.setMessage(message);
    notificationForm.setTitle(title);
    notificationForm.setRefType(ITDreamConstant.NOTIFICATION_TYPE_REVIEW_SUBMISSION);
    notificationForm.setRefId(refId);
    notificationService.notifyStudent(notificationForm);
  }
}
