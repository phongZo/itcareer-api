package com.base.auth.model.criteria;

import com.base.auth.model.Simulation;
import com.base.auth.model.Task;
import com.base.auth.model.TaskQuestion;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class TaskQuestionCriteria {
  @NotNull(message = "simulationId required")
  private Long simulationId;
  @NotNull(message = "taskId required")
  private Long taskId;
  private Long educatorId;
  private Integer status;

  public Specification<TaskQuestion> getSpecification() {
    return new Specification<TaskQuestion>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(Root<TaskQuestion> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        Join<TaskQuestion, Task> taskJoin = root.join("task");
        Join<Task, Simulation> simulationJoin = taskJoin.join("simulation");

        predicates.add(cb.equal(taskJoin.get("id"), taskId));
        predicates.add(cb.equal(simulationJoin.get("id"), simulationId));

        if (getEducatorId() != null){
          predicates.add(cb.equal(simulationJoin.get("educator").get("id"), educatorId));
        }

        if (getStatus()!=null){
          predicates.add(cb.equal(simulationJoin.get("status"), getStatus()));
        }
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
      }
    };
  }
}
