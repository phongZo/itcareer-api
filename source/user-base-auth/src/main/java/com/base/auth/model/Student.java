package com.base.auth.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "db_it_dream_student")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Student{
  @Id
  private Long id;
  @OneToOne
  @MapsId
  @JoinColumn(name = "id")
  private Account account;
}
