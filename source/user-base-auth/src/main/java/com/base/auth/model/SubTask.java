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
@Table(name = "db_user_base_sub_task")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SubTask{
  @Id
  @GenericGenerator(name = "idGenerator", strategy = "com.base.auth.service.id.IdGenerator")
  @GeneratedValue(generator = "idGenerator")
  private Long id;
  private String title;
  @Column(columnDefinition = "TEXT")
  private String introduction;
  @Column(columnDefinition = "LONGTEXT")
  private String content;
  private String imagePath;
  private String filePath;
  private String videoPath;
  @ManyToOne
  @JoinColumn(name = "task_id")
  private Task task;
}
