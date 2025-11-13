package com.base.auth.repository;

import com.base.auth.model.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Boolean existsByReceiverIdAndRefId(Long receiverId, Long refId);

  List<Notification> findTop20ByReceiverIdOrderByCreatedDateDesc(long studentId);

  void deleteByReceiverIdAndRefId(Long studentId, Long reviewSubmissionId);
}
