package com.base.auth.repository;

import com.base.auth.model.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

  void deleteByStudentId(Long studentId);

  void deleteBySimulationId(Long simulationId);

  int countBySimulationId(Long simulationId);

  List<Review> findAllByStudentId(Long studentId);
}
