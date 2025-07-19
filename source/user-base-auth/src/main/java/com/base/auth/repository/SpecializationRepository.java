package com.base.auth.repository;

import com.base.auth.model.Specialization;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpecializationRepository extends JpaRepository<Specialization, Long>,
    JpaSpecificationExecutor<Specialization> {

  Optional<Specialization> findByName(String name);
}
