package com.example.todoapi.notification.job;

import java.util.ArrayList;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.todoapi.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

/**
 * 期限直前の“未通知”タスクを見つけて、通知を作成（= notified_at を埋める）
 */
@Component
@RequiredArgsConstructor
public class NotificationJob {
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 60_000) // 1分ごと
    public void run() {
        var sentIds = new ArrayList<Long>();
        for (var t : notificationService.collectDueTodos()) { // 通知対象の検出
            try {
                sentIds.add(t.getId());
            } catch (Exception e) {
                // ログのみ出して継続（失敗分は次回また拾う＝重複の可能性はあるが欠落しにくい）
            }
        }
        notificationService.markNotifiedByIds(sentIds); // まとめて既読化
    }

}
