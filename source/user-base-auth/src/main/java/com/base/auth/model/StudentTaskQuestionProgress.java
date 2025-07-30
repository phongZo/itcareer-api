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
@Table(name = "db_user_base_student_task_question_progress")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class StudentTaskQuestionProgress {
  @Id
  @GenericGenerator(name = "idGenerator", strategy = "com.base.auth.service.id.IdGenerator")
  @GeneratedValue(generator = "idGenerator")
  private  Long id;
  @ManyToOne
  @JoinColumn(name = "student_subtask_progress_id")
  private StudentSubTaskProgress studentSubTaskProgress;
  @ManyToOne
  @JoinColumn(name = "task_question_id")
  private TaskQuestion taskQuestion;
  @Column(name = "answer", columnDefinition = "TEXT")
  private String answer;
  private Boolean isCorrect;
}
