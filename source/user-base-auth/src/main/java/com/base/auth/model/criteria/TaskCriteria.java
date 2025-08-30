package com.base.auth.model.criteria;

import com.base.auth.model.Educator;
import com.base.auth.model.Simulation;
import com.base.auth.model.Task;
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
public class TaskCriteria {
  @NotNull(message = "simulationId is required")
  private Long simulationId;
  private Long educatorId;
  private Integer status;
  private Long parentId;

  public Specification<Task> getSpecification() {
    return new Specification<Task>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        Join<Task, Simulation> joinSimulation = root.join("simulation", JoinType.INNER);
        predicates.add(cb.equal(joinSimulation.get("id"), getSimulationId()));

        if (getParentId() != null){
          Join<Task, Task> taskJoin = root.join("parent", JoinType.INNER);
          predicates.add(cb.equal(taskJoin.get("id"), getParentId()));
        }

        if (getEducatorId() != null) {
          Join<Simulation, Educator> joinEducator = joinSimulation.join("educator", JoinType.INNER);
          predicates.add(cb.equal(joinEducator.get("id"), getEducatorId()));
        }

        if (getStatus() != null){
          predicates.add(cb.equal(joinSimulation.get("status"), getStatus()));
        }

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
      }
    };
  }
}
