package com.example.todoapi.push.service;

import com.example.todoapi.push.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/** Web Push実送信（PushServiceにペイロードを渡して各端末へ送る） */
@Service
@RequiredArgsConstructor
public class WebPushSender {
    private final PushService pushService; // WebPushConfigクラスでカスタマイズされたインスタンスが自動注入される
    private final PushSubscriptionRepository repo;

    /* ユーザー全端末に送信。何か1つでも成功すれば true */
    public int sendToUser(Long userId, Long todoId, String title, String body, String url) {
        var subs = repo.findByUserId(userId); // ユーザーの購読一覧を取得
        int delivered = 0;
        String tag = (todoId != null) ? ("todo-" + todoId) : "todo-general";

        for (var s : subs) {
            try {
                var json = """
                        {"title": %s, "body": %s, "url": %s, "todoId": %s, "tag": %s, "renotify": true}
                        """.formatted(quote(title), quote(body), quote(url != null ? url : "/"),
                        (todoId != null ? String.valueOf(todoId) : "null"), quote(tag));
                var n = new Notification(s.getEndpoint(), s.getP256dh(), s.getAuth(),
                        json.getBytes(StandardCharsets.UTF_8)); // 上記jsonペイロードを含めた通知を作成

                var resp = pushService.send(n); // エンドポイント（ユーザーが使用するブラウザのService Worker）にHTTP POSTを実行
                int code = resp.getStatusLine().getStatusCode();
                String respBody = (resp.getEntity() != null) ? EntityUtils.toString(resp.getEntity()) : "";
                // ★ 成否カウントはコードを見てから
                if (code == 201 || code == 202) {
                    delivered++;
                } else {
                    // 410/404 は購読が死んでいる可能性が高い（最小限の後始末）
                    if (code == 404 || code == 410) {
                        try {
                            repo.deleteByUserIdAndEndpoint(userId, s.getEndpoint());
                        } catch (Exception ignore) {
                        }
                    }
                }
                System.out.printf("[webpush] endpoint=%s code=%d body=%s%n", s.getEndpoint(), code, respBody);
            } catch (Exception e) {
                System.err.printf("[notify] push failed endpoint=%s err=%s%n", s.getEndpoint(), e.toString());
            }
        }
        return delivered;
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
