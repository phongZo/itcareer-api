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

  Optional<StudentSubTaskProgress> findByTaskIdAndStudentId(Long taskId, long studentId);

  void deleteAllByStudentId(Long studentId);

  @Modifying
  @Transactional
  @Query(value = "DELETE sstp FROM db_user_base_student_subtask_progress sstp " +
      "WHERE sstp.task_id = :taskId " +
      "OR sstp.task_id IN (SELECT id FROM db_user_base_task WHERE parent_id = :taskId)", nativeQuery = true)
  void deleteAllByTaskAndSubtask(@Param("taskId") Long taskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE sstp FROM db_user_base_student_subtask_progress sstp " +
          "JOIN db_user_base_task t ON sstp.task_id = t.id " +
          "WHERE t.simulation_id = :simulationId", nativeQuery = true)
  void deleteAllBySimulationId(@Param("simulationId") Long simulationId);

  //  @Modifying
//  @Transactional
//  @Query(value = "DELETE sstp FROM db_user_base_student_subtask_progress sstp " +
//      "JOIN db_user_base_sub_task st ON sstp.subtask_id = st.id " +
//      "JOIN db_user_base_task t ON st.task_id = t.id " +
//      "JOIN db_user_base_simulation sim ON t.simulation_id = sim.id " +
//      "JOIN db_user_base_educator edu ON sim.educator_id = edu.id " +
//      "WHERE edu.id = :educatorId", nativeQuery = true)
//  void deleteAllByEducatorId(@Param("educatorId") Long educatorId);

  @Modifying
  @Transactional
  @Query(value = "DELETE sstp FROM db_user_base_student_subtask_progress sstp " +
          "JOIN db_user_base_task t ON sstp.task_id = t.id " +
          "JOIN db_user_base_simulation sim ON t.simulation_id = sim.id " +
          "WHERE sim.educator_id = :educatorId", nativeQuery = true)
  void deleteAllByEducatorId(@Param("educatorId") Long educatorId);
}
