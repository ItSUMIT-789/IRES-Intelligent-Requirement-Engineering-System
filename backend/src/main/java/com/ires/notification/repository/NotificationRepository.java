package com.ires.notification.repository;
import com.ires.notification.entity.Notification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface NotificationRepository extends JpaRepository<Notification,UUID>{
 Page<Notification> findByUserId(UUID userId,Pageable pageable);
 Page<Notification> findByUserIdAndRead(UUID userId,boolean read,Pageable pageable);
 Optional<Notification> findByIdAndUserId(UUID id,UUID userId);
 long countByUserIdAndReadFalse(UUID userId);
 List<Notification> findByUserIdAndReadFalse(UUID userId);
}
