package com.base.auth.repository;

import com.base.auth.model.StudentTaskQuestionProgress;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentTaskQuestionProgressRepository extends JpaRepository<StudentTaskQuestionProgress, Long>,
    JpaSpecificationExecutor<StudentTaskQuestionProgress> {


  void deleteAllByStudentSubTaskProgressId(Long studentSubTaskProgressId);

  Optional<StudentTaskQuestionProgress> findFirstByStudentSubTaskProgressId(Long studentSubTaskProgressId);

  @Modifying
  @Transactional
  @Query(value = "DELETE stq FROM db_user_base_student_task_question_progress stq " +
      "JOIN db_user_base_student_subtask_progress ss ON stq.student_subtask_progress_id = ss.id " +
      "WHERE ss.student_id = :studentId", nativeQuery = true)
  void deleteAllByStudentId(Long studentId);

  @Modifying
  @Transactional
  @Query("DELETE FROM StudentTaskQuestionProgress stq WHERE stq.taskQuestion.id = :taskQuestionId")
  void deleteAllByTaskQuestionId(@Param("taskQuestionId") Long taskQuestionId);

  @Query("SELECT COUNT(stq) FROM StudentTaskQuestionProgress stq " +
      "WHERE stq.studentSubTaskProgress.id = :studentSubTaskProgressId AND stq.isCorrect = true")
  int countCorrectByStudentSubTaskProgressId(@Param("studentSubTaskProgressId") Long studentSubTaskProgressId);

  Optional<StudentTaskQuestionProgress> findByTaskQuestionIdAndStudentSubTaskProgressIdAndIsCorrect(Long taskQuestionId, Long studentSubTaskProgressId, boolean isCorrect);

  @Modifying
  @Transactional
  @Query(value = "DELETE stqp FROM db_user_base_student_task_question_progress stqp " +
      "JOIN db_user_base_task_question tq ON stqp.task_question_id = tq.id " +
      "WHERE tq.sub_task_id = :subTaskId", nativeQuery = true)
  void deleteAllBySubTaskId(@Param("subTaskId") Long subTaskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE stqp FROM db_user_base_student_task_question_progress stqp " +
      "JOIN db_user_base_task_question tq ON stqp.task_question_id = tq.id " +
      "JOIN db_user_base_sub_task st ON tq.sub_task_id = st.id " +
      "WHERE st.task_id = :taskId", nativeQuery = true)
  void deleteAllByTaskId(@Param("taskId") Long taskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE stqp FROM db_user_base_student_task_question_progress stqp " +
      "JOIN db_user_base_task_question tq ON stqp.task_question_id = tq.id " +
      "JOIN db_user_base_sub_task st ON tq.sub_task_id = st.id " +
      "JOIN db_user_base_task t ON st.task_id = t.id " +
      "WHERE t.simulation_id = :simulationId", nativeQuery = true)
  void deleteAllBySimulationId(@Param("simulationId") Long simulationId);

  @Modifying
  @Transactional
  @Query(value = "DELETE stqp FROM db_user_base_student_task_question_progress stqp " +
      "JOIN db_user_base_task_question tq ON stqp.task_question_id = tq.id " +
      "JOIN db_user_base_sub_task st ON tq.sub_task_id = st.id " +
      "JOIN db_user_base_task t ON st.task_id = t.id " +
      "JOIN db_user_base_simulation sim ON t.simulation_id = sim.id " +
      "JOIN db_user_base_educator edu ON sim.educator_id = edu.id " +
      "WHERE edu.id = :educatorId", nativeQuery = true)
  void deleteAllByEducatorId(@Param("educatorId") Long educatorId);
}
