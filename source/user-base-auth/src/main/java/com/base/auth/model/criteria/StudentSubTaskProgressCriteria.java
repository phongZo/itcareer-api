package com.base.auth.model.criteria;

import com.base.auth.model.Student;
import com.base.auth.model.StudentSubTaskProgress;
import com.base.auth.model.SubTask;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class StudentSubTaskProgressCriteria {
  private Long studentId;
  private Long subtaskId;

  public Specification<StudentSubTaskProgress> getSpecification() {
    return new Specification<StudentSubTaskProgress>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(Root<StudentSubTaskProgress> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (getStudentId() != null){
          Join<StudentSubTaskProgress, Student> studentJoin = root.join("student");
          predicates.add(cb.equal(studentJoin.get("id"), getStudentId()));
        }

        if (getSubtaskId() != null){
          Join<StudentSubTaskProgress, SubTask> subTaskJoin = root.join("subTask");
          predicates.add(cb.equal(subTaskJoin.get("id"), getSubtaskId()));
        }
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
      }
    };
  }
}
