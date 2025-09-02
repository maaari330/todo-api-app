package com.example.todoapi.push.web;

import com.example.todoapi.push.service.PushSubscriptionService;
import com.example.todoapi.service.CustomUserDetailsService.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** webpush購読の登録/解除API */
@RestController
@RequestMapping("/push")
@RequiredArgsConstructor
public class PushController {

    private final PushSubscriptionService service;

    // ブラウザが返してくる購読JSONの受け口
    public record SubscriptionDto(String endpoint, Keys keys) {
        public record Keys(String p256dh, String auth) {
        }
    }

    /* 購読登録 */
    @PostMapping("/subscribe")
    public void subscribe(@AuthenticationPrincipal LoginUser user,
            @RequestBody SubscriptionDto dto,
            @RequestHeader(value = "User-Agent", required = false) String ua) { // User-Agent：ブラウザやHTTPクライアントの製品名/バージョン、OS/プラットフォームなど
        service.save(user.getId(), dto.endpoint(), dto.keys().p256dh(), dto.keys().auth(), ua);
    }

    /* 購読解除 */
    @DeleteMapping("/unsubscribe")
    public void unsubscribe(@AuthenticationPrincipal LoginUser user,
            @RequestBody SubscriptionDto dto) {
        service.deleteByEndpoint(user.getId(), dto.endpoint());
    }
}