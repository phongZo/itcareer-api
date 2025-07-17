package com.base.auth.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "db_user_base_student")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Student{
  @Id
  @GenericGenerator(name = "idGenerator", strategy = "com.base.auth.service.id.IdGenerator")
  @GeneratedValue(generator = "idGenerator")
  private Long id;
  @OneToOne
  @JoinColumn(name = "account_id")
  private Account account;
  private Date birthday;
}
