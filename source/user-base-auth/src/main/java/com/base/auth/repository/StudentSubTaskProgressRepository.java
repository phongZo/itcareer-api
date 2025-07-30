package com.base.auth.repository;

import com.base.auth.model.StudentSubTaskProgress;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentSubTaskProgressRepository extends JpaRepository<StudentSubTaskProgress, Long>,
    JpaSpecificationExecutor<StudentSubTaskProgress> {

  Optional<StudentSubTaskProgress> findBySubTaskIdAndStudentId(Long subTaskId, long studentId);

  void deleteAllByStudentId(Long studentId);

  Optional<StudentSubTaskProgress> findFirstBySubTaskId(Long subTaskId);
}
