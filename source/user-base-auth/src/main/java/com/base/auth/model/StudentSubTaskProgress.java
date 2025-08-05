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
@Table(name = "db_user_base_student_subtask_progress")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class StudentSubTaskProgress extends Auditable<String>{
  @Id
  @GenericGenerator(name = "idGenerator", strategy = "com.base.auth.service.id.IdGenerator")
  @GeneratedValue(generator = "idGenerator")
  private Long id;
  @ManyToOne
  @JoinColumn(name = "student_id")
  private Student student;
  @ManyToOne
  @JoinColumn(name = "task_id")
  private Task task;
  private Integer currentAttempt = 1;
  private Integer errorCount = 0;
  private Integer state;
}
