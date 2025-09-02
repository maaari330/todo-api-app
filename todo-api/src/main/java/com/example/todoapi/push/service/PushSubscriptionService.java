package com.example.todoapi.push.service;

import com.example.todoapi.push.entity.PushSubscription;
import com.example.todoapi.push.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 購読情報の保存/削除ロジック */
@Service
@RequiredArgsConstructor
public class PushSubscriptionService {
    private final PushSubscriptionRepository repo;

    /* 購読情報をリポジトリに保存 */
    @Transactional
    public void save(Long userId, String endpoint, String p256dh, String auth, String userAgent) {
        // 既存（同一endpoint）の重複を避ける
        var existing = repo.findByUserIdAndEndpoint(userId, endpoint);
        if (existing.isPresent())
            return;

        var s = new PushSubscription();
        s.setUserId(userId);
        s.setEndpoint(endpoint);
        s.setP256dh(p256dh);
        s.setAuth(auth);
        s.setUserAgent(userAgent);
        repo.save(s);
    }

    /* useridとendpointをキーに購読情報を削除 */
    @Transactional
    public void deleteByEndpoint(Long userId, String endpoint) {
        repo.deleteByUserIdAndEndpoint(userId, endpoint);
    }
}
