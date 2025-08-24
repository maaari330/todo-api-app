package com.example.todoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.todoapi.entity.PushSubscription;
import java.util.List;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUserId(Long userId);
}
