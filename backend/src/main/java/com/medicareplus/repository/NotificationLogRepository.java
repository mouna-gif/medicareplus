package com.medicareplus.repository;
import com.medicareplus.domain.NotificationLog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Page<NotificationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
