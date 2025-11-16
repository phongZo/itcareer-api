package com.base.auth.repository;

import com.base.auth.model.Review;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
  void deleteBySimulationId(Long simulationId);

  int countBySimulationId(Long simulationId);

  List<Review> findAllByStudentId(Long studentId);

  boolean existsByStudentIdAndSimulationId(long studentId, Long simulationId);

  @Modifying
  @Transactional
  @Query(
      value = "DELETE r " +
          "FROM db_it_dream_review r " +
          "JOIN db_it_dream_simulation s ON r.simulation_id = s.id " +
          "WHERE s.educator_id = :educatorId",
      nativeQuery = true
  )
  void deleteAllByEducatorId(@Param("educatorId") Long educatorId);
}
