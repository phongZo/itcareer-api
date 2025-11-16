package com.base.auth.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "db_it_dream_review_submission")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ReviewSubmission extends Auditable<String>{
  @Id
  @GenericGenerator(name = "idGenerator", strategy = "com.base.auth.service.id.IdGenerator")
  @GeneratedValue(generator = "idGenerator")
  private Long id;
  @ManyToOne
  @JoinColumn(name = "simulation_id")
  private Simulation simulation;
  @ManyToOne
  @JoinColumn(name = "student_id")
  private Student student;
  @Column(columnDefinition = "TEXT")
  private String content;
  private Boolean isReviewed = true;
}
