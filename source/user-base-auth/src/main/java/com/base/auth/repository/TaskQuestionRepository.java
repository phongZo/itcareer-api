package com.base.auth.repository;

import com.base.auth.model.TaskQuestion;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TaskQuestionRepository extends JpaRepository<TaskQuestion, Long>,
    JpaSpecificationExecutor<TaskQuestion> {

  Optional<TaskQuestion> findByQuestion(String question);

  Optional<TaskQuestion> findByOptions(String options);

  void deleteBySubTaskId(Long subTaskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE tq FROM db_user_base_task_question tq " +
      "JOIN db_user_base_sub_task s ON tq.sub_task_id = s.id " +
      "WHERE s.task_id = :taskId", nativeQuery = true)
  void deleteAllTaskQuestionByTaskId(Long taskId);

  @Modifying
  @Transactional
  @Query(value = "DELETE tq FROM db_user_base_task_question tq " +
      "JOIN db_user_base_sub_task s ON tq.sub_task_id = s.id " +
      "JOIN db_user_base_task t ON s.task_id = t.id " +
      "WHERE t.simulation_id = :simulationId", nativeQuery = true)
  void deleteAllTaskQuestionBySimulationId(Long simulationId);
}
