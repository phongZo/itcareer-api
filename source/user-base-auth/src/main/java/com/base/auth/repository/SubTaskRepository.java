package com.base.auth.repository;

import com.base.auth.model.SubTask;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SubTaskRepository extends JpaRepository<SubTask, Long>, JpaSpecificationExecutor<SubTask> {

  Optional<SubTask> findByTitleAndTaskId(String title, Long taskId);

  void deleteByTaskId(Long taskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE s FROM db_user_base_sub_task s " +
      "JOIN db_user_base_task t ON s.task_id = t.id " +
      "WHERE t.simulation_id = :simulationId", nativeQuery = true)
  void deleteAllSubTaskBySimulationId(Long simulationId);
}
