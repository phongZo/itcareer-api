package com.base.auth.repository;

import com.base.auth.model.Simulation;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SimulationRepository extends JpaRepository<Simulation, Long>,
    JpaSpecificationExecutor<Simulation> {

  Optional<Simulation> findBySpecializationId(Long specializationId);

  Page<Simulation> findAllByStatus(Integer statusActive, Pageable pageable);

  Optional<Simulation> findByTitleAndEducatorId(String title, long educatorId);

  void deleteAllByEducatorId(Long educatorId);

  List<Simulation> findAllByEducatorId(Long educatorId);
}
