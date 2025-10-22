package com.example.todoapi.push.web;

import com.example.todoapi.push.entity.PushSubscription;
import com.example.todoapi.push.service.PushSubscriptionService;
import com.example.todoapi.push.service.WebPushSender;
import com.example.todoapi.repository.UserRepository;
import com.example.todoapi.entity.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * /api/push 配下。
 * Nginx のリバプロで /api/ が Spring Boot に流れる前提。
 */
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {

    private final PushSubscriptionService subs;
    private final UserRepository userRepository;

    /** 購読登録 (ブラウザからの subscription をそのまま送る) */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody SubscribeReq req, Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();
        Long ownerId = userRepository.findByUsername(principal.getName())
                .map(User::getId).orElse(null);
        if (ownerId == null)
            return ResponseEntity.status(401).build();

        PushSubscription saved = subs.upsert(ownerId, req.endpoint, req.keys.p256dh, req.keys.auth);
        return ResponseEntity.ok(saved.getId());
    }

    /** 購読解除 */
    @DeleteMapping("/subscribe")
    public ResponseEntity<?> unsubscribe(@RequestParam("endpoint") String endpoint, Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();
        // 実装ポイント: endpoint が ownerId に紐づく場合のみ削除するようにサービ層でチェック
        subs.unsubscribeOwned(principal.getName(), endpoint);
        return ResponseEntity.noContent().build();
    }

    // ------- DTO --------
    @Data
    public static class SubscribeReq {
        @NotBlank
        public String endpoint;
        @NotNull
        public Keys keys;
    }

    @Data
    public static class Keys {
        @NotBlank
        public String p256dh; // Base64URL
        @NotBlank
        public String auth; // Base64URL
    }
}
