package com.base.auth.model.criteria;

import com.base.auth.model.StudentSubTaskProgress;
import com.base.auth.model.StudentTaskQuestionProgress;
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
public class StudentTaskQuestionProgressCriteria {
  @NotNull(message = "studentSubTaskProgressId required")
  private Long studentSubTaskProgressId;
  @NotNull(message = "subTaskId required")
  private Long subTaskId;
  private Long studentId;
  private Long taskQuestionId;
  private Boolean isCorrect;

  public Specification<StudentTaskQuestionProgress> getSpecification() {
    return new Specification<StudentTaskQuestionProgress>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(Root<StudentTaskQuestionProgress> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        Join<StudentTaskQuestionProgress, StudentSubTaskProgress> progressJoin = root.join("studentSubTaskProgress");
        predicates.add(cb.equal(progressJoin.get("id"), getStudentSubTaskProgressId()));
        predicates.add(cb.equal(progressJoin.get("subTask").get("id"), getSubTaskId()));

        if (getStudentId() != null){
          predicates.add(cb.equal(progressJoin.get("student").get("id"), getStudentId()));
        }

        if (getIsCorrect() != null){
          predicates.add(cb.equal(root.get("isCorrect"), getIsCorrect()));
        }

        if (getTaskQuestionId() != null){
          Join<StudentTaskQuestionProgress, TaskQuestion> taskQuestionJoin = progressJoin.join("taskQuestion");
          predicates.add(cb.equal(taskQuestionJoin.get("id"), getTaskQuestionId()));
        }
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
      }
    };
  }
}
