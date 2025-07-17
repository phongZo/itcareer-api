package com.base.auth.repository;

import com.base.auth.model.Educator;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EducatorRepository extends JpaRepository<Educator, Long>,
    JpaSpecificationExecutor<Educator> {

  Optional<Educator> findByAccountId(Long accountId);
}
