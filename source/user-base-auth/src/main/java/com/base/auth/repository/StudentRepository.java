package com.base.auth.repository;

import com.base.auth.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

  @Transactional
  @Modifying
  void deleteAllByAccountId(Long accountId);

  @Query("SELECT s FROM Student s " +
      "WHERE (SELECT COUNT(DISTINCT sp) FROM StudentSubTaskProgress sp JOIN sp.task t " +
      "       WHERE sp.student = s AND t.simulation.id = :simulationId AND sp.state = :state) = :totalTasks")
  Page<Student> findStudentsCompletedSimulation(
      @Param("simulationId") Long simulationId,
      @Param("state") Integer state,
      @Param("totalTasks") Long totalTasks,
      Pageable pageable);
}
