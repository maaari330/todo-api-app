package com.example.todoapi.push.service;

import com.example.todoapi.push.entity.PushSubscription;
import com.example.todoapi.push.dto.SubscribeRequest;
import com.example.todoapi.push.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {
    private final PushSubscriptionRepository repo;

    /** 購読情報の登録 */
    @Transactional
    public void upsert(Long userId, SubscribeRequest req) {
        var existing = repo.findByUserIdAndEndpoint(userId, req.endpoint()).orElse(null);
        if (existing == null) {
            var s = new PushSubscription();
            s.setUserId(userId);
            s.setEndpoint(req.endpoint());
            s.setP256dh(req.p256dh());
            s.setAuth(req.auth());
            s.setUserAgent(req.userAgent());
            repo.save(s);
        } else {
            existing.setP256dh(req.p256dh());
            existing.setAuth(req.auth());
            existing.setUserAgent(req.userAgent());
            repo.save(existing);
        }
    }

    /** 購読情報の削除 */
    @Transactional
    public void unsubscribe(Long userId, String endpoint) {
        repo.deleteByUserIdAndEndpoint(userId, endpoint);
    }

    public List<PushSubscription> listForUser(Long userId) {
        return repo.findAllByUserId(userId);
    }
}