package com.example.todoapi.push.service;

import com.example.todoapi.push.entity.PushSubscription;
import com.example.todoapi.push.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {
    private final PushSubscriptionRepository repo;

    @Transactional
    public PushSubscription upsert(Long ownerId, String endpoint, String p256dh, String auth) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId must not be null");
        }
        return repo.findByEndpoint(endpoint)
                .map(s -> {
                    s.setOwnerId(ownerId);
                    s.setP256dh(p256dh);
                    s.setAuth(auth);
                    return s;
                })
                .orElseGet(() -> repo.save(
                        PushSubscription.builder()
                                .ownerId(ownerId)
                                .endpoint(endpoint)
                                .p256dh(p256dh)
                                .auth(auth)
                                .build()));
    }

    @Transactional
    public void unsubscribeOwned(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId must not be null");
        }
        repo.deleteByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public List<PushSubscription> listForOwner(Long ownerId) {
        return repo.findByOwnerId(ownerId);
    }
}