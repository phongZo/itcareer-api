package com.base.auth.repository;

import com.base.auth.dto.reviewSubmission.ReviewedStudentProjection;
import com.base.auth.model.ReviewSubmission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewSubmissionRepository extends JpaRepository<ReviewSubmission, Long> {

  Optional<ReviewSubmission> findBySimulationIdAndStudentId(Long simulationId, Long studentId);

  @Query("SELECT r FROM ReviewSubmission r " +
      "WHERE r.simulation.id = :simulationId " +
      "AND r.student.account.username = :username")
  Optional<ReviewSubmission> findBySimulationIdAndStudentUsername(
      @Param("simulationId") Long simulationId,
      @Param("username") String username);

  boolean existsBySimulationIdAndStudentId(Long simulationId, Long studentId);

  @Query("select new com.base.auth.dto.reviewSubmission.ReviewedStudentProjection(" +
      "rs.student.account.username, rs.isReviewed) " +
      "from ReviewSubmission rs " +
      "where rs.simulation.id = :simulationId")
  List<ReviewedStudentProjection> findReviewedStudentUsernamesBySimulationId(@Param("simulationId") Long simulationId);
}
