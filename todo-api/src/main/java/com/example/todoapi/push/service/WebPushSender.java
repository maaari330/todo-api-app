package com.example.todoapi.push.service;

import com.example.todoapi.push.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/** Web Push実送信（PushServiceにペイロードを渡して各端末へ送る） */
@Service
@RequiredArgsConstructor
public class WebPushSender {
    private final PushService pushService; // WebPushConfigクラスでカスタマイズされたインスタンスが自動注入される
    private final PushSubscriptionRepository repo;

    /* ユーザー全端末に送信。何か1つでも成功すれば true */
    public boolean sendToUser(Long userId, Long todoId, String title, String body, String url) {
        var subs = repo.findByUserId(userId); // ユーザーの購読一覧を取得
        boolean any = false;
        String tag = (todoId != null) ? ("todo-" + todoId) : "todo-general";

        for (var s : subs) {
            try {
                var json = """
                        {"title": %s, "body": %s, "url": %s, "todoId": %s, "tag": %s, "renotify": true}
                        """.formatted(quote(title), quote(body), quote(url != null ? url : "/"),
                        (todoId != null ? String.valueOf(todoId) : "null"), quote(tag));
                var n = new Notification(s.getEndpoint(), s.getP256dh(), s.getAuth(),
                        json.getBytes(StandardCharsets.UTF_8)); // 上記jsonペイロードを含めた通知を作成
                pushService.send(n); // エンドポイント（ユーザーが使用するブラウザのService Worker）にHTTP POSTを実行
                any = true;
            } catch (Exception e) {
            }
        }
        return any;
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
