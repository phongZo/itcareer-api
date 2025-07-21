package com.base.auth.model.criteria;

import com.base.auth.model.Specialization;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

@Data
public class SpecializationCriteria {
  private String name;
  public Specification<Specialization> getSpecification() {
    return new Specification<Specialization>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(Root<Specialization> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (!StringUtils.isBlank(getName()))
        {
          predicates.add(cb.like(cb.lower(root.get("name")),"%"+ getName()+"%"));
        }

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
      }
    };
  }
}
