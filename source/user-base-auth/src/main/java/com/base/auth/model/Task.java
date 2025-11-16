package com.base.auth.model;

import com.base.auth.constant.ITDreamConstant;
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
@Table(name = "db_it_dream_task")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Task{
  @Id
  @GenericGenerator(name = "idGenerator", strategy = "com.base.auth.service.id.IdGenerator")
  @GeneratedValue(generator = "idGenerator")
  private Long id;
  private String name;
  @Column(columnDefinition = "TEXT")
  private String description;
  private String title;
  @Column(columnDefinition = "TEXT")
  private String introduction;
  @Column(columnDefinition = "TEXT")
  private String content;
  private String imagePath;
  private String filePath;
  private String videoPath;
  private Integer state = ITDreamConstant.STATE_TASK_INIT;
  private Integer maxErrors = 0;
  private Integer totalQuestion = 0;
  private Integer kind;
  @ManyToOne
  @JoinColumn(name = "parent_id")
  private Task parent;
  @ManyToOne
  @JoinColumn(name = "simulation_id")
  private Simulation simulation;
}
