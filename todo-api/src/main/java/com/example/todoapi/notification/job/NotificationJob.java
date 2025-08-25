package com.example.todoapi.notification.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.todoapi.notification.service.InAppNotificationQueue;
import com.example.todoapi.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationJob {
    private final NotificationService notificationService;
    private final InAppNotificationQueue inApp;

    @Scheduled(fixedDelay = 60_000) // 1分ごと
    public void run() {
        var sentIds = new java.util.ArrayList<Long>();
        for (var t : notificationService.collectDueTodos()) {
            try {
                Long uid = 1L; // 実装に合わせて取得
                String title = "まもなく期限";
                String body = t.getTitle();
                inApp.push(new InAppNotificationQueue.InAppMessage(
                        t.getId(), title + "：「" + body + "」", t.getDueDate(), uid));
                sentIds.add(t.getId()); // 送れたものだけ記録
            } catch (Exception e) {
                // ログに出して継続（このタスクは次回また送る＝重複の可能性はあるが欠落はしない）
            }
        }
        notificationService.markNotifiedByIds(sentIds); // まとめて既読化
    }
}
