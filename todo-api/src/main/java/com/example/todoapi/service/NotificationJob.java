package com.example.todoapi.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.todoapi.entity.Todo;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationJob {
    private final NotificationService notificationService;
    private final InAppNotificationQueue inApp;
    private final WebPushSender webPushSender;

    // ユーザーIDの取得方法：Todo に userId があるなら t.getUser().getId() に置換
    private Long resolveUserIdForTodo(Todo t) {
        // 例: ひとまず単一ユーザー運用なら 1L 固定。実装に合わせて変更。
        return 1L;
    }

    @Scheduled(fixedDelay = 60_000) // 1分ごと
    public void run() {
        for (var t : notificationService.collectDueTodosAndMarkNotified()) {
            Long uid = resolveUserIdForTodo(t);
            String title = "まもなく期限";
            String body = t.getTitle();
            String url = "/"; // 例: タスク詳細ページに飛ばすなら /todos/<id>

            // アプリ内
            inApp.push(
                    new InAppNotificationQueue.InAppMessage(t.getId(), title + "：「" + body + "」", t.getDueDate(), uid));
            // Push
            webPushSender.sendToUser(uid, title, body, url);
        }
    }
}
