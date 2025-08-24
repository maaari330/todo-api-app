package com.example.todoapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todoapi.service.InAppNotificationQueue;
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
    public List<InAppNotificationQueue.InAppMessage> recent() {
        return queue.recent();
    }
}
