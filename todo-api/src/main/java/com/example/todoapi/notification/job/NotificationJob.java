package com.example.todoapi.notification.job;

import java.util.ArrayList;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.todoapi.notification.service.NotificationService;
import com.example.todoapi.push.service.WebPushSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/** Web Push の「Push型」通知を1分毎の @Scheduled で検出→送信→送信済み確定まで行う定期ジョブ */
@Slf4j
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
        log.info("NotificationJob: found {} due todos", targets.size());
        var successIds = new ArrayList<Long>();
        for (var t : targets) {
            try{
                // 通知対象の検出
                int delivered = webPushSender.sendToUser(
                        t.getOwner().getId(),
                        t.getId(),
                        "My TODO Task Manager",
                        "まもなく期限",
                        "「" + t.getTitle() + "」が近づいています",
                        "/app/todos/" + t.getId());
                // if (delivered > 0) {
                    successIds.add(t.getId()); // 送れたものだけ確定
                // }
            }catch(Exception e){
                log.warn("WebPush送信に失敗しました userId={}, todoId={}",
                        t.getOwner().getId(), t.getId(), e);
            }

        }
        if (!successIds.isEmpty()) {
            log.info("NotificationJob: markNotifiedByIds {}", successIds);
            notificationService.markNotifiedByIds(successIds); // 通知送付済みに変更
        }else {
            log.info("NotificationJob: no successIds; markNotifiedByIds is not called");
        }
    }
}