package com.example.todoapi.notification.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.example.todoapi.notification.service.InAppNotificationQueue;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final InAppNotificationQueue queue;

    /**
     * 認証ユーザーのアプリ内通知の直近5件をInAppNotificationQueueクラスから取り出して返すエンドポイント
     */
    @GetMapping("/in-app/recent")
    public List<InAppNotificationQueue.InAppMessage> recent(@AuthenticationPrincipal(expression = "id") Long userId) {
        return queue.recent();
    }
}
