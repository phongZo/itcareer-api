package com.base.auth.repository;

import com.base.auth.model.StudentSubTaskProgress;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentSubTaskProgressRepository extends JpaRepository<StudentSubTaskProgress, Long>,
    JpaSpecificationExecutor<StudentSubTaskProgress> {

  Optional<StudentSubTaskProgress> findBySubTaskIdAndStudentId(Long subTaskId, long studentId);

  void deleteAllByStudentId(Long studentId);

  @Modifying
  @Transactional
  @Query("DELETE FROM StudentSubTaskProgress s WHERE s.subTask.id = :subTaskId")
  void deleteAllBySubTaskId(@Param("subTaskId") Long subTaskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE sstp FROM db_user_base_student_subtask_progress sstp " +
      "JOIN db_user_base_sub_task st ON sstp.subtask_id = st.id " +
      "WHERE st.task_id = :taskId", nativeQuery = true)
  void deleteAllByTaskId(@Param("taskId") Long taskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE sstp FROM db_user_base_student_subtask_progress sstp " +
      "JOIN db_user_base_sub_task st ON sstp.subtask_id = st.id " +
      "JOIN db_user_base_task t ON st.task_id = t.id " +
      "WHERE t.simulation_id = :simulationId", nativeQuery = true)
  void deleteAllBySimulationId(@Param("simulationId") Long simulationId);

  @Modifying
  @Transactional
  @Query(value = "DELETE sstp FROM db_user_base_student_subtask_progress sstp " +
      "JOIN db_user_base_sub_task st ON sstp.subtask_id = st.id " +
      "JOIN db_user_base_task t ON st.task_id = t.id " +
      "JOIN db_user_base_simulation sim ON t.simulation_id = sim.id " +
      "JOIN db_user_base_educator edu ON sim.educator_id = edu.id " +
      "WHERE edu.id = :educatorId", nativeQuery = true)
  void deleteAllByEducatorId(@Param("educatorId") Long educatorId);
}
