package com.base.auth.repository;

import com.base.auth.model.StudentTaskQuestionProgress;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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

  Optional<StudentTaskQuestionProgress> findFirstByTaskQuestionId(Long taskQuestionId);
}
