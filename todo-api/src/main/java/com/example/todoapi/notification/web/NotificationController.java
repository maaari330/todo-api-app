package com.example.todoapi.notification.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.todoapi.notification.service.NotificationService;
import com.example.todoapi.notification.service.NotificationService.InAppMessage;
import com.example.todoapi.notification.service.NotificationService.Paged;
import com.example.todoapi.service.CustomUserDetailsService.LoginUser;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import java.time.ZoneOffset;

/**
 * 通知ドメインサービス：通知対象の検出・状態更新・in-app 用ページング取得など通知ロジックの中核
 * アプリ内通知はユーザーがサイト画面を見てるときに最新通知を表示するため、ユーザーが見る時にこのクラスをPullする
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    /** ログインユーザーのアプリ内通知を、afterIso 以降（未指定時は直近 minutesFallback 分）からページング取得して返す */
    @GetMapping("/in-app/recent")
    public Paged<InAppMessage> recent(
            @AuthenticationPrincipal LoginUser user,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "20") int size, // ← 1ページの件数、デフォルト20件
            @RequestParam(required = false) String afterIso, // ← フロントが「最後に見た時刻」（ISO形式）を送れる口
            @RequestParam(required = false, defaultValue = "1440") int minutesFallback // after が無いときの保険、「遡る幅」
    ) {
        System.out.println("InApp API called. user.id = " + user.getId());
        Instant after = (afterIso != null && !afterIso.isBlank())
                ? Instant.parse(afterIso)
                : Instant.now().minus(Duration.ofMinutes(minutesFallback));
        LocalDateTime since = LocalDateTime.ofInstant(after, ZoneOffset.UTC);
        return notificationService.findRecentByUserPaged(user.getId(), since, page, size);
    }
}
