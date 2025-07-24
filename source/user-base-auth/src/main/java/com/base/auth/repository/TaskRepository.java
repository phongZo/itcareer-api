package com.base.auth.repository;

import com.base.auth.model.Task;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
  Optional<Task> findByNameAndSimulationId(String name, Long simulationId);

  Optional<Task> findBySimulationId(Long simulationId);
}
