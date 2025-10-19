package com.example.todoapi.push.web;

import com.example.todoapi.push.entity.PushSubscription;
import com.example.todoapi.push.service.PushSubscriptionService;
import com.example.todoapi.push.service.WebPushSender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * /api/push 配下。
 * Nginx のリバプロで /api/ が Spring Boot に流れる前提。
 */
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {

    private final PushSubscriptionService subs;
    private final WebPushSender sender;

    /** 購読登録 (ブラウザからの subscription をそのまま送る) */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody SubscribeReq req, Principal principal) {
        Long ownerId = extractOwnerId(principal); // 認証無しなら null でも可
        subs.upsert(ownerId, req.endpoint, req.keys.p256dh, req.keys.auth);
        return ResponseEntity.noContent().build();
    }

    /** 購読解除 */
    @DeleteMapping("/subscribe")
    public ResponseEntity<?> unsubscribe(@RequestParam("endpoint") String endpoint) {
        subs.unsubscribe(endpoint);
        return ResponseEntity.noContent().build();
    }

    /** 動作確認用：対象ユーザーにダミー送信 */
    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestParam(name = "ownerId", required = false) Long ownerId) {
        List<PushSubscription> list = (ownerId == null)
                ? subs.listForOwner(null)
                : subs.listForOwner(ownerId);

        int ok = 0, ng = 0;
        var payload = new WebPushSender.PushPayload(
                "Hello from TODO",
                "This is a test push",
                "/",
                "/icons/icon-192.png",
                null, // todoId なし
                ownerId // ownerId が無ければ null でOK
        );

        for (PushSubscription s : list) {
            boolean delivered = sender.send(s.getEndpoint(), s.getP256dh(), s.getAuth(), payload);
            if (delivered)
                ok++;
            else
                ng++;
        }
        return ResponseEntity.ok().body("{\"ok\":" + ok + ",\"ng\":" + ng + "}");
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

    private Long extractOwnerId(Principal principal) {
        // 認証を使っていれば principal から取り出して返す
        // 使っていないなら null を返してもOK
        return null;
    }
}
