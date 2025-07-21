package com.base.auth.model;

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
@Table(name = "db_user_base_simulation")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Simulation extends Auditable<String>{
  @Id
  @GenericGenerator(name = "idGenerator", strategy = "com.base.auth.service.id.IdGenerator")
  @GeneratedValue(generator = "idGenerator")
  private Long id;
  private String title;
  private String overview;
  private String description;
  private Integer level;
  private String totalEstimatedTime = "0";
  private String imagePath;
  private String videoPath;
  private Float avgRating = 0F;
  private Integer participantQuantity = 0;
  @ManyToOne
  @JoinColumn(name = "specialization_id")
  private Specialization specialization;
  @ManyToOne
  @JoinColumn(name = "educator_id")
  private Educator educator;
}
