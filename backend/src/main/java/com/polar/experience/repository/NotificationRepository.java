package com.polar.experience.repository;

import com.polar.experience.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientRoleOrderByCreatedAtDesc(String recipientRole);

    long countByRecipientRoleAndReadFlagFalse(String recipientRole);
}
