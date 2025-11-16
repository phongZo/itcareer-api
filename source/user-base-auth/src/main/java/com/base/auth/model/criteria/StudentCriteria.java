package com.base.auth.model.criteria;

import com.base.auth.model.Account;
import com.base.auth.model.Student;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

@Data
public class StudentCriteria {
  private Long id;
  private String fullName;
  private String phone;
  private String email;
  private Integer status;

  public Specification<Student> getSpecification() {
    return new Specification<Student>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(Root<Student> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if(getId()!=null)
        {
          predicates.add(cb.equal(root.get("id"),getId()));
        }
        if(getStatus()!=null)
        {
          Join<Student, Account> joinAccount = root.join("account", JoinType.INNER);
          predicates.add(cb.equal(joinAccount.get("status"),getStatus()));
        }
        if (StringUtils.isNotBlank(getPhone()))
        {
          Join<Student, Account> joinAccount = root.join("account", JoinType.INNER);
          predicates.add(cb.like(cb.lower(joinAccount.get("phone")),"%"+ getPhone()+"%"));
        }
        if (StringUtils.isNotBlank(getEmail()))
        {
          Join<Student, Account> joinAccount = root.join("account",JoinType.INNER);
          predicates.add(cb.like(cb.lower(joinAccount.get("email")),"%"+ getEmail()+"%"));
        }
        if (StringUtils.isNotBlank(getFullName()))
        {
          Join<Student, Account> joinAccount = root.join("account",JoinType.INNER);
          predicates.add(cb.like(cb.lower(joinAccount.get("fullName")),"%"+ getFullName()+"%"));
        }

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
      }
    };
  }
}
