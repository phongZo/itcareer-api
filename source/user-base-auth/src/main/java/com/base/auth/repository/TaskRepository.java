package com.base.auth.repository;

import com.base.auth.model.Task;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
  Optional<Task> findByNameAndSimulationId(String name, Long simulationId);

  void deleteBySimulationId(Long id);

  @Modifying
  @Transactional
  @Query(value = "DELETE t FROM db_user_base_task t " +
      "JOIN db_user_base_simulation s ON t.simulation_id = s.id " +
      "WHERE s.educator_id = :educatorId", nativeQuery = true)
  void deleteAllByEducatorId(@Param("educatorId") Long educatorId);

}
