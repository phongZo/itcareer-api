package com.base.auth.controller;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.achievement.AchievementDisplayDto;
import com.base.auth.dto.studentSubTaskProgress.StudentSubTaskProgressDisplayDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.studentSubTaskProgress.RequestStudentSubTaskProgressForm;
import com.base.auth.mapper.StudentSubTaskProgressMapper;
import com.base.auth.model.Achievement;
import com.base.auth.model.Simulation;
import com.base.auth.model.Student;
import com.base.auth.model.StudentSubTaskProgress;
import com.base.auth.model.StudentTaskQuestionProgress;
import com.base.auth.model.Task;
import com.base.auth.repository.AchievementRepository;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.StudentRepository;
import com.base.auth.repository.StudentSubTaskProgressRepository;
import com.base.auth.repository.StudentTaskQuestionProgressRepository;
import com.base.auth.repository.TaskQuestionRepository;
import com.base.auth.repository.TaskRepository;
import java.util.Objects;
import javax.transaction.Transactional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
  TaskRepository taskRepository;

  @Autowired
  StudentRepository studentRepository;

  @Autowired
  StudentTaskQuestionProgressRepository studentTaskQuestionProgressRepository;

  @Autowired
  SimulationRepository simulationRepository;

  @Autowired
  AchievementRepository achievementRepository;

  @Autowired
  TaskQuestionRepository taskQuestionRepository;

  @GetMapping(value = "/student-get/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STSP_ST_V')")
  public ApiMessageDto<StudentSubTaskProgressDisplayDto> getForStudent(@PathVariable("taskId") Long taskId){
    ApiMessageDto<StudentSubTaskProgressDisplayDto> apiMessageDto = new ApiMessageDto<>();
    Student student = studentRepository.findById(getCurrentUser()).orElseThrow(()
        -> new NotFoundException("Student not found", ErrorCode.USER_ERROR_NOT_FOUND));
    if (!isStudent()){
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    Task task = taskRepository.findById(taskId).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    Simulation simulation = task.getSimulation();
    if (simulation == null){
      throw new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND);
    }
    // Tiến trình khi truy cập vào task
    if (Objects.equals(task.getKind(), ITDreamConstant.TASK_KIND_TASK)){
      apiMessageDto.setMessage("Get task success");
      return apiMessageDto;
    }

    // Tiến trình khi truy cập vào subtask
    boolean existTaskQuestion = taskQuestionRepository.existsByTaskId(task.getId());
    StudentSubTaskProgress existStudentSubTaskProgress = studentSubTaskProgressRepository.findByTaskIdAndStudentId(
        task.getId(), getCurrentUser()).orElse(null);
    if (existStudentSubTaskProgress != null){
      if (!existTaskQuestion){
        existStudentSubTaskProgress.setState(ITDreamConstant.STATE_STUDENT_SUBTASK_PROGRESS_COMPLETED);
      }
      existStudentSubTaskProgress.setCurrentAttempt(existStudentSubTaskProgress.getCurrentAttempt() + 1);
      studentSubTaskProgressRepository.save(existStudentSubTaskProgress);
      apiMessageDto.setData(studentSubTaskProgressMapper.fromEntityToStudentSubTaskProgressDisplayDto(existStudentSubTaskProgress));
      apiMessageDto.setMessage("Get student subtask progress success");
    } else {
      StudentSubTaskProgress studentSubTaskProgress = new StudentSubTaskProgress();
      studentSubTaskProgress.setStudent(student);
      studentSubTaskProgress.setTask(task);
      if (!existTaskQuestion){
        studentSubTaskProgress.setState(ITDreamConstant.STATE_STUDENT_SUBTASK_PROGRESS_COMPLETED);
      } else {
        studentSubTaskProgress.setState(ITDreamConstant.STATE_STUDENT_SUBTASK_PROGRESS_IN_PROGRESS);
      }
      studentSubTaskProgressRepository.save(studentSubTaskProgress);
      simulation.setParticipantQuantity(simulation.getParticipantQuantity() + 1);
      simulationRepository.save(simulation);
      apiMessageDto.setData(studentSubTaskProgressMapper.fromEntityToStudentSubTaskProgressDisplayDto(studentSubTaskProgress));
      apiMessageDto.setMessage("Create student subtask progress success");
    }
    return apiMessageDto;
  }

  @PutMapping(value = "/complete", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STSP_ST_CPL')")
  @Transactional
  public ApiMessageDto<AchievementDisplayDto> complete(@Valid @RequestBody RequestStudentSubTaskProgressForm requestStudentSubTaskProgressForm, BindingResult bindingResult){
    ApiMessageDto<AchievementDisplayDto> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not an student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    Task task = taskRepository.findById(requestStudentSubTaskProgressForm.getTaskId()).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));

    StudentSubTaskProgress studentSubTaskProgress = studentSubTaskProgressRepository.findByTaskIdAndStudentId(task.getId(), getCurrentUser()).orElseThrow(()
    -> new NotFoundException("Student subtask progress not found",
        ErrorCode.STUDENT_SUBTASK_PROGRESS_ERROR_NOT_FOUND));

    int count = studentTaskQuestionProgressRepository.countCorrectByStudentSubTaskProgressId(studentSubTaskProgress.getId());
    if (count != task.getTotalQuestion()){
      throw new BadRequestException("Student subtask progress cannot be completed", ErrorCode.STUDENT_SUBTASK_PROGRESS_ERROR_NOT_COMPLETED);
    }

    studentSubTaskProgress.setState(ITDreamConstant.STATE_STUDENT_SUBTASK_PROGRESS_COMPLETED);
    studentSubTaskProgress.setErrorCount(ITDreamConstant.RESTART_ERROR_COUNT);

    StudentTaskQuestionProgress studentTaskQuestionProgress = studentTaskQuestionProgressRepository
        .findFirstByStudentSubTaskProgressId(studentSubTaskProgress.getId()).orElse(null);

    if (studentTaskQuestionProgress != null && Objects.equals(studentTaskQuestionProgress.getTaskQuestion().getQuestionType(), ITDreamConstant.QUESTION_TYPE_MULTIPLE_CHOICE)){
      studentTaskQuestionProgressRepository.deleteAllByStudentSubTaskProgressId(studentSubTaskProgress.getId());
    }
    studentSubTaskProgressRepository.save(studentSubTaskProgress);

    // Đếm số lượng subtask coi có phải là hoàn thành được subtask cuối trong task hay không
    Long countSubtask = taskRepository.countByKindAndParentId(ITDreamConstant.TASK_KIND_SUBTASK, task.getId());
    Long countProgressInTask = studentSubTaskProgressRepository.countByStateAndStudentIdAndTaskKindAndTaskParentId(
        ITDreamConstant.STATE_STUDENT_SUBTASK_PROGRESS_COMPLETED, getCurrentUser(), ITDreamConstant.TASK_KIND_SUBTASK, task.getId());
    // Đếm số lượng subtask trong simulation coi có phải là hoàn thành được subtask cuối trong simulation hay không
    Long countSubtaskInSimulation = taskRepository.countByKindAndSimulationId(ITDreamConstant.TASK_KIND_SUBTASK ,task.getSimulation().getId());
    // Đếm số lượng tất cả tiến trình đã được hoàn thành trong simulation hiện tại
    Long countStudentSubTaskProgress = studentSubTaskProgressRepository.countByStateAndStudentIdAndTaskSimulationId(
        ITDreamConstant.STATE_STUDENT_SUBTASK_PROGRESS_COMPLETED, getCurrentUser(), task.getSimulation().getId());

    if (Objects.equals(countSubtask, countProgressInTask)){
      if (!Objects.equals(countSubtaskInSimulation, countStudentSubTaskProgress)){
        apiMessageDto.setMessage("Complete task");
        return apiMessageDto;
      } else {
        Student student = studentRepository.findById(getCurrentUser()).orElseThrow(()
            -> new NotFoundException("Student not found", ErrorCode.USER_ERROR_NOT_FOUND));
        boolean existAchievement = achievementRepository.existsByStudentIdAndSimulationId(student.getId(), task.getSimulation().getId());
        if (!existAchievement){
          Achievement achievement = new Achievement();
          achievement.setSimulation(task.getSimulation());
          achievement.setStudent(student);
          achievementRepository.save(achievement);

          AchievementDisplayDto achievementDisplayDto = new AchievementDisplayDto();
          achievementDisplayDto.setId(achievement.getId());
          achievementDisplayDto.setUsername(student.getAccount().getUsername());
          achievementDisplayDto.setSimulationName(task.getSimulation().getTitle());
          apiMessageDto.setData(achievementDisplayDto);
          apiMessageDto.setMessage("Complete simulation");
          return apiMessageDto;
        }
      }
    }
    apiMessageDto.setMessage("Complete subtask progress");
    return apiMessageDto;
  }

  @PutMapping(value = "/restart", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('STSP_ST_RES')")
  @Transactional
  public ApiMessageDto<String> restart(@Valid @RequestBody RequestStudentSubTaskProgressForm requestStudentSubTaskProgressForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not an student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    Task task = taskRepository.findById(requestStudentSubTaskProgressForm.getTaskId()).orElseThrow(()
    -> new NotFoundException("Task not found", ErrorCode.TASK_ERROR_NOT_FOUND));
    StudentSubTaskProgress studentSubTaskProgress = studentSubTaskProgressRepository.findByTaskIdAndStudentId(task.getId(), getCurrentUser()).orElseThrow(()
        -> new NotFoundException("Student subtask progress not found", ErrorCode.STUDENT_SUBTASK_PROGRESS_ERROR_NOT_FOUND));
    studentSubTaskProgress.setCurrentAttempt(studentSubTaskProgress.getCurrentAttempt() + 1);
    studentSubTaskProgress.setErrorCount(ITDreamConstant.RESTART_ERROR_COUNT);
    studentSubTaskProgress.setState(ITDreamConstant.STATE_STUDENT_SUBTASK_PROGRESS_IN_PROGRESS);
    studentSubTaskProgress.setStatus(ITDreamConstant.STATUS_ACTIVE);
    studentTaskQuestionProgressRepository.deleteAllByStudentSubTaskProgressId(studentSubTaskProgress.getId());
    studentSubTaskProgressRepository.save(studentSubTaskProgress);
    apiMessageDto.setMessage("Restart student subtask progress success");
    return apiMessageDto;
  }
}
