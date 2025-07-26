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
@Table(name = "db_user_base_task_question")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class TaskQuestion {
  @Id
  @GenericGenerator(name = "idGenerator", strategy = "com.base.auth.service.id.IdGenerator")
  @GeneratedValue(generator = "idGenerator")
  private Long id;
  @Column(name = "question", columnDefinition = "TEXT")
  private String question;
  private Integer questionType;
  @Column(name = "options" ,  columnDefinition = "TEXT")
  private String options;
  @ManyToOne
  @JoinColumn(name = "sub_task_id")
  private SubTask subTask;
}
