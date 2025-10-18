package com.example.todoapi.notification.job;

import java.util.ArrayList;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.todoapi.notification.service.NotificationService;
import com.example.todoapi.push.service.WebPushSender;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Web Push の「Push型」通知を1分毎の @Scheduled で検出→送信→送信済み確定まで行う定期ジョブ */
@Component
@RequiredArgsConstructor
public class NotificationJob {
    private final NotificationService notificationService;
    private final WebPushSender webPushSender;

    @Scheduled(fixedDelayString = "${notify.scan-ms:60000}") // 1分ごと
    @Transactional
    public void run() {
        var targets = notificationService.collectDueTodos();
        if (targets.isEmpty())
            return;
        var successIds = new ArrayList<Long>();
        for (var t : targets) {
            // 通知対象の検出
            int delivered = webPushSender.sendToUser(
                    t.getOwner().getId(),
                    t.getId(),
                    "まもなく期限",
                    "「" + t.getTitle() + "」が近づいています",
                    "/app/todos/" + t.getId());
            System.out.printf("[notify] todoId=%d ownerId=%d delivered=%d due=%s offset=%d%n",
                    t.getId(), t.getOwner().getId(), delivered, t.getDueDate(), t.getRemindOffsetMinutes());
            if (delivered > 0) {
                successIds.add(t.getId()); // 送れたものだけ確定
            }
        }
        if (!successIds.isEmpty()) {
            notificationService.markNotifiedByIds(successIds); // 通知送付済みに変更
        }
    }
}