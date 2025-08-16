package com.base.auth.model.criteria;

import com.base.auth.model.Educator;
import com.base.auth.model.Review;
import com.base.auth.model.Simulation;
import com.base.auth.model.Specialization;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class ReviewCriteria {
  @NotNull(message = "simulationId required")
  private Long simulationId;

  public Specification<Review> getSpecification() {
    return new Specification<Review>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(Root<Review> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        Join<Review, Simulation> simulationJoin = root.join("simulation", JoinType.INNER);
        predicates.add(cb.equal(simulationJoin.get("id"), getSimulationId()));
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
      }
    };
  }
}
