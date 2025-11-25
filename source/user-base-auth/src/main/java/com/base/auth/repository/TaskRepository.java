package com.base.auth.repository;

import com.base.auth.model.Task;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM db_it_dream_task WHERE parent_id IS NOT NULL AND simulation_id = :simulationId", nativeQuery = true)
  void deleteAllSubTaskBySimulationId(@Param("simulationId") Long simulationId);

  @Modifying
  @Transactional
  @Query(value = "DELETE FROM db_it_dream_task WHERE parent_id IS NULL AND simulation_id = :simulationId", nativeQuery = true)
  void deleteAllTaskBySimulationId(@Param("simulationId") Long simulationId);

  @Modifying
  @Transactional
  @Query(value =
      "DELETE FROM db_it_dream_task " +
          "WHERE parent_id IS NOT NULL AND simulation_id IN (SELECT id FROM db_it_dream_simulation WHERE educator_id = :educatorId)",
      nativeQuery = true)
  void deleteAllSubTaskByEducatorId(@Param("educatorId") Long educatorId);

  @Modifying
  @Transactional
  @Query(value =
      "DELETE FROM db_it_dream_task " +
          "WHERE parent_id IS NULL AND simulation_id IN (SELECT id FROM db_it_dream_simulation WHERE educator_id = :educatorId)",
      nativeQuery = true)
  void deleteAllTaskByEducatorId(@Param("educatorId") Long educatorId);

  void deleteAllByParentId(Long id);


  Optional<Task> findByIdAndKind(Long parentId, Integer taskKindTask);

  Boolean existsByNameAndKindAndSimulationId(String name, Integer taskKindTask, Long simulationId);


  List<Task> findAllByParentId(Long id);

  List<Task> findAllBySimulationId(Long simulationId);

  @Query("SELECT t FROM Task t " +
      "JOIN t.simulation s " +
      "JOIN s.educator e " +
      "WHERE e.id = :educatorId")
  List<Task> findAllByEducatorId(@Param("educatorId") Long educatorId);

  Long countBySimulationId(Long simulationId);

  Boolean existsByTitleAndKindAndParentIdAndSimulationId(String title, Integer taskKindSubtask, Long parentId, Long simulationId);

  Long countByKindAndSimulationId(Integer taskKindTask, Long id);

  boolean existsByKindAndId(Integer taskKindTask, Long parentId);
}
