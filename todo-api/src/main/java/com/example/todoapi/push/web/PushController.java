package com.example.todoapi.push.web;

import com.example.todoapi.push.config.VapidProperties;
import com.example.todoapi.push.dto.SubscribeRequest;
import com.example.todoapi.push.service.PushSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.nio.file.attribute.UserPrincipal;

/**
 * /api/push 配下。
 * Nginx のリバプロで /api/ が Spring Boot に流れる前提。
 */
@RestController
@RequestMapping("/push")
@RequiredArgsConstructor
public class PushController {
    private final PushSubscriptionService service;
    private final VapidProperties vapid;

    /** 公開鍵の取得 */
    @GetMapping("/public-key")
    public ResponseEntity<String> publicKey() {
        return ResponseEntity.ok(vapid.getPublicKey());
    }

    /** 購読登録 */
    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@AuthenticationPrincipal UserPrincipal user,
            @RequestBody SubscribeRequest req, @AuthenticationPrincipal(expression = "id") Long userId) {
        service.upsert(userId, req);
        return ResponseEntity.ok().build();
    }

    /** 購読解除 */
    @DeleteMapping("/subscribe")
    public ResponseEntity<Void> unsubscribe(@AuthenticationPrincipal UserPrincipal user,
            @RequestParam("endpoint") String endpoint, @AuthenticationPrincipal(expression = "id") Long userId) {
        service.unsubscribe(userId, endpoint);
        return ResponseEntity.noContent().build();
    }
}
