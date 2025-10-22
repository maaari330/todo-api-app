package com.example.todoapi.push.repository;

import com.example.todoapi.push.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** push_subscriptions のCRUD（JPAリポジトリ） */
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    Optional<PushSubscription> findByUserIdAndEndpoint(Long userId, String endpoint);

    List<PushSubscription> findAllByUserId(Long userId);

    void deleteByUserIdAndEndpoint(Long userId, String endpoint);

}