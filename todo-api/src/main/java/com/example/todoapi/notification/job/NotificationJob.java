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
    private final WebPushSender webPushSender; // WebPushSender.javaから取得

    @Scheduled(fixedDelay = 60_000) // 1分ごと
    @Transactional
    public void run() {
        var targets = notificationService.collectDueTodos();
        if (targets.isEmpty())
            return;
        var sentIds = new ArrayList<Long>();
        for (var t : targets) { // 通知対象の検出
            webPushSender.sendToUser(
                    t.getOwner().getId(),
                    "まもなく期限",
                    "「" + t.getTitle() + "」が近づいています",
                    "/app/todos/" + t.getId());
            sentIds.add(t.getId());
        }
        notificationService.markNotifiedByIds(sentIds); // まとめて既読化
    }
}
