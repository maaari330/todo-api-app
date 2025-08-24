package com.example.todoapi.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;

import com.example.todoapi.repository.PushSubscriptionRepository;
import com.example.todoapi.entity.PushSubscription;
import java.util.Map;

@RestController
@RequestMapping("/notifications/webpush")
@RequiredArgsConstructor
public class WebPushController {
    @Value("${notifications.webpush.publicKey:}")
    String publicKey;
    private final PushSubscriptionRepository repo;

    @GetMapping("/vapidPublicKey")
    public Map<String, String> vapid() {
        return Map.of("key", publicKey);
    }

    @PostMapping("/subscribe")
    public void subscribe(@AuthenticationPrincipal(expression = "id") Long userId,
            @RequestBody Map<String, Object> sub) {
        // ↑ カスタムUserPrincipalにidがない場合は、SecurityContextから取り出す等に変更
        if (userId == null)
            userId = 1L; // 仮: 未実装時の単一ユーザー運用
        @SuppressWarnings("unchecked")
        Map<String, String> keys = (Map<String, String>) ((Map<?, ?>) sub.get("keys"));
        var ps = new PushSubscription();
        ps.setUserId(userId);
        ps.setEndpoint((String) sub.get("endpoint"));
        ps.setP256dh(keys.get("p256dh"));
        ps.setAuth(keys.get("auth"));
        // 同一endpointはユニーク制約。既存なら無視。
        try {
            repo.save(ps);
        } catch (Exception ignored) {
        }
    }
}
