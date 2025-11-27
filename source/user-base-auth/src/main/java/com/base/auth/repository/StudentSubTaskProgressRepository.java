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
  @Query(value = "DELETE sstp FROM db_it_dream_student_subtask_progress sstp " +
      "WHERE sstp.task_id = :taskId " +
      "OR sstp.task_id IN (SELECT id FROM db_it_dream_task WHERE parent_id = :taskId)", nativeQuery = true)
  void deleteAllByTaskAndSubtask(@Param("taskId") Long taskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE sstp FROM db_it_dream_student_subtask_progress sstp " +
          "JOIN db_it_dream_task t ON sstp.task_id = t.id " +
          "WHERE t.simulation_id = :simulationId", nativeQuery = true)
  void deleteAllBySimulationId(@Param("simulationId") Long simulationId);

  @Modifying
  @Transactional
  @Query(value = "DELETE sstp FROM db_it_dream_student_subtask_progress sstp " +
          "JOIN db_it_dream_task t ON sstp.task_id = t.id " +
          "JOIN db_it_dream_simulation sim ON t.simulation_id = sim.id " +
          "WHERE sim.educator_id = :educatorId", nativeQuery = true)
  void deleteAllByEducatorId(@Param("educatorId") Long educatorId);

  boolean existsByStudentIdAndTaskSimulationId(long studentId, Long simulationId);

  Long countByStateAndStudentIdAndTaskSimulationId(Integer state, long studentId, Long simulationId);

  @Query("SELECT COUNT(sstp) " +
      "FROM StudentSubTaskProgress sstp " +
      "JOIN sstp.task t " +
      "WHERE sstp.student.id = :studentId " +
      "AND t.simulation.id = :simulationId " +
      "AND sstp.state = :state " +
      "AND t.kind = :kind")
  Long countByStateAndStudentIdAndTaskSimulationIdAndTaskKind(
      @Param("state") Integer state,
      @Param("studentId") Long studentId,
      @Param("simulationId") Long simulationId,
      @Param("kind") Integer kind);
}
