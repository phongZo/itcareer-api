package com.base.auth.model.criteria;

import com.base.auth.model.Educator;
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
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

@Data
public class SimulationCriteria {
  private String title;
  private Long specializationId;
  private Long educatorId;
  private Integer level;

  public Specification<Simulation> getSpecification() {
    return new Specification<Simulation>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(Root<Simulation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(getTitle()))
        {
          predicates.add(cb.like(cb.lower(root.get("title")),"%"+ getTitle()+"%"));
        }

        if (getLevel() != null){
          predicates.add(cb.equal(root.get("level"), getLevel()));
        }

        if (getSpecializationId() != null){
          Join<Simulation, Specialization> specializationJoin = root.join("specialization", JoinType.INNER);
          predicates.add(cb.equal(specializationJoin.get("id"), getSpecializationId()));
        }

        if (getEducatorId() != null){
          Join<Simulation, Educator> educatorJoin = root.join("educator", JoinType.INNER);
          predicates.add(cb.equal(educatorJoin.get("id"), getEducatorId()));
        }
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
      }
    };
  }
}
