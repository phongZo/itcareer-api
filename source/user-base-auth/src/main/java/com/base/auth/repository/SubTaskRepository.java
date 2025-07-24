package com.base.auth.repository;

import com.base.auth.model.SubTask;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubTaskRepository extends JpaRepository<SubTask, Long>, JpaSpecificationExecutor<SubTask> {

  Optional<SubTask> findByTitleAndTaskId(String title, Long taskId);

  void deleteByTaskId(Long taskId);
}
