package com.example.todoapi.push.repository;

import com.example.todoapi.push.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** push_subscriptions のCRUD（JPAリポジトリ） */
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByOwnerId(Long userId);

    Optional<PushSubscription> findByEndpoint(String endpoint);

    void deleteByOwnerIdAndEndpoint(Long ownerId, String endpoint);
}