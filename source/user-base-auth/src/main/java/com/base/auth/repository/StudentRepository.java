package com.base.auth.repository;

import com.base.auth.model.Account;
import com.base.auth.model.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

  Optional<Student> findByAccountId(Long accountId);

  @Transactional
  @Modifying
  void deleteAllByAccountId(Long accountId);

  boolean existsByAccountId(long currentUser);
}
