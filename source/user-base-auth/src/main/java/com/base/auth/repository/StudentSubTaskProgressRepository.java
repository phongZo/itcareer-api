package com.base.auth.repository;

import com.base.auth.model.StudentSubTaskProgress;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentSubTaskProgressRepository extends JpaRepository<StudentSubTaskProgress, Long>,
    JpaSpecificationExecutor<StudentSubTaskProgress> {

  Optional<StudentSubTaskProgress> findBySubTaskIdAndStudentId(Long subTaskId, long studentId);

  void deleteAllByStudentId(Long studentId);

  Optional<StudentSubTaskProgress> findFirstBySubTaskId(Long subTaskId);

  @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
      "FROM StudentSubTaskProgress s " +
      "JOIN s.subTask st " +
      "JOIN st.task t " +
      "WHERE t.simulation.id = :simulationId")
  boolean existsStudentSubTaskProgressBySimulationId(@Param("simulationId") Long simulationId);

  @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
      "FROM StudentSubTaskProgress s " +
      "JOIN s.subTask st " +
      "WHERE st.task.id = :taskId")
  boolean existsStudentSubTaskProgressByTaskId(@Param("taskId") Long taskId);

  @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
      "FROM StudentSubTaskProgress s " +
      "JOIN s.subTask st " +
      "JOIN st.task t " +
      "JOIN t.simulation sim " +
      "WHERE sim.educator.id = :educatorId")
  boolean existsStudentSubTaskProgressByEducatorId(@Param("educatorId") Long educatorId);
}
