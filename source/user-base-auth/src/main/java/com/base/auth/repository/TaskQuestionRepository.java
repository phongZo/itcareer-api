package com.base.auth.repository;

import com.base.auth.model.TaskQuestion;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskQuestionRepository extends JpaRepository<TaskQuestion, Long>,
    JpaSpecificationExecutor<TaskQuestion> {
  @Modifying
  @Transactional
  @Query(value =
      "DELETE FROM db_it_dream_task_question tq " +
          "WHERE " +
          "   tq.task_id = :taskId " +
          "   OR (" +
          "       (SELECT kind FROM db_it_dream_task WHERE id = :taskId) = 1 " +
          "       AND tq.task_id IN (SELECT id FROM db_it_dream_task WHERE parent_id = :taskId)" +
          "   )",
      nativeQuery = true)
  void deleteAllByTaskAndSubtask(@Param("taskId") Long taskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE tq FROM db_it_dream_task_question tq " +
          "JOIN db_it_dream_task t ON tq.task_id = t.id " +
          "WHERE t.simulation_id = :simulationId", nativeQuery = true)
  void deleteAllBySimulationId(@Param("simulationId") Long simulationId);

  @Modifying
  @Transactional
  @Query(value = "DELETE tq FROM db_it_dream_task_question tq " +
      "JOIN db_it_dream_task t ON tq.task_id = t.id " +
      "JOIN db_it_dream_simulation s ON t.simulation_id = s.id " +
      "WHERE s.educator_id = :educatorId", nativeQuery = true)
  void deleteAllByEducatorId(@Param("educatorId") Long educatorId);

  Optional<TaskQuestion> findByQuestionAndTaskId(String question, Long taskId);

  TaskQuestion findFirstByTaskId(Long taskId);


  boolean existsByOptions(String options);

  boolean existsByQuestionAndOptionsAndTaskId(String question, String options, Long taskId);

  boolean existsByQuestionAndTaskId(String question, Long taskId);

  boolean existsByTaskId(Long taskId);
}
