package com.base.auth.repository;

import com.base.auth.model.StudentTaskQuestionProgress;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  void deleteAllByTaskQuestionId(Long taskQuestionId);

  @Query("SELECT COUNT(stq) FROM StudentTaskQuestionProgress stq " +
      "WHERE stq.studentSubTaskProgress.id = :studentSubTaskProgressId AND stq.isCorrect = true")
  int countCorrectByStudentSubTaskProgressId(@Param("studentSubTaskProgressId") Long studentSubTaskProgressId);

  Optional<StudentTaskQuestionProgress> findByTaskQuestionIdAndStudentSubTaskProgressIdAndIsCorrect(Long taskQuestionId, Long studentSubTaskProgressId, boolean isCorrect);

  @Modifying
  @Transactional
  @Query(value = "DELETE stqp FROM db_user_base_student_task_question_progress stqp " +
          "WHERE stqp.task_question_id IN ( " +
          "   SELECT tq.id FROM db_user_base_task_question tq " +
          "   WHERE tq.task_id = :taskId " +
          "   OR tq.task_id IN (SELECT id FROM db_user_base_task WHERE parent_id = :taskId))", nativeQuery = true)
  void deleteAllByTaskAndSubtask(@Param("taskId") Long taskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE stqp FROM db_user_base_student_task_question_progress stqp " +
          "JOIN db_user_base_task_question tq ON stqp.task_question_id = tq.id " +
          "JOIN db_user_base_task t ON tq.task_id = t.id " +
          "WHERE t.simulation_id = :simulationId", nativeQuery = true)
  void deleteAllBySimulationId(@Param("simulationId") Long simulationId);

  @Modifying
  @Transactional
  @Query(value = "DELETE stqp FROM db_user_base_student_task_question_progress stqp " +
          "JOIN db_user_base_task_question tq ON stqp.task_question_id = tq.id " +
          "JOIN db_user_base_task t ON tq.task_id = t.id " +
          "JOIN db_user_base_simulation sim ON t.simulation_id = sim.id " +
          "WHERE sim.educator_id = :educatorId", nativeQuery = true)
  void deleteAllByEducatorId(@Param("educatorId") Long educatorId);

  @Query("SELECT stq FROM StudentTaskQuestionProgress stq " +
      "JOIN stq.studentSubTaskProgress sstp " +
      "JOIN stq.taskQuestion tq " +
      "JOIN tq.task t " +
      "WHERE sstp.student.id = :studentId AND t.simulation.id = :simulationId " +
      "ORDER BY t.id, tq.id")
  Page<StudentTaskQuestionProgress> findAllByStudentIdAndSimulationId(
      @Param("studentId") Long studentId,
      @Param("simulationId") Long simulationId,
      Pageable pageable);
}
