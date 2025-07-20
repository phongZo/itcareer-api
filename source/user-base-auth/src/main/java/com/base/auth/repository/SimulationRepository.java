package com.base.auth.repository;

import com.base.auth.model.Simulation;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SimulationRepository extends JpaRepository<Simulation, Long>,
    JpaSpecificationExecutor<Simulation> {

  Optional<Simulation> findBySpecializationId(Long specializationId);

  Optional<Simulation> findByTitle(String title);

  Page<Simulation> findAllByStatus(Integer statusActive, Pageable pageable);

  Page<Simulation> findAllByEducatorId(Long educatorId, Pageable pageable);
}
