package com.example.todoapi.push.service;

import com.example.todoapi.push.entity.PushSubscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushSender {

    private final PushService pushService;
    private final PushSubscriptionService subscriptionService;
    private final ObjectMapper om = new ObjectMapper();

    /** 単一購読に送る低レベル API（成功: true） */
    public boolean send(String endpoint, String p256dh, String auth, PushPayload payload) {
        try {
            String json = om.writeValueAsString(payload);
            Notification n = new Notification(endpoint, p256dh, auth, json);
            pushService.send(n);
            return true;
        } catch (Exception e) {
            log.warn("[webpush] failed endpoint={} err={}", endpoint, e.toString());
            return false;
        }
    }

    /**
     * 指定ユーザーの全購読に push を送信し、成功件数を返す。
     * NotificationJob から呼ぶ公開API（シグネチャはジョブ側に合わせてあります）
     */
    public int sendToUser(Long userId, Long todoId, String title, String body, String url) {
        // ownerId が null の場合は匿名購読などへ送る実装に変えてもOK
        List<PushSubscription> subs = subscriptionService.listForUser(userId);
        if (subs.isEmpty()) {
            log.debug("[webpush] no subscriptions ownerId={}", userId);
            return 0;
        }

        int delivered = 0;
        PushPayload payload = new PushPayload(
                title,
                body,
                (url != null ? url : "/"),
                "/icons/icon-192.png",
                todoId,
                userId);

        for (PushSubscription s : subs) {
            boolean ok = send(s.getEndpoint(), s.getP256dh(), s.getAuth(), payload);
            if (ok)
                delivered++;
            else {
                // 410 Gone 等の恒久失敗なら購読削除も検討（ライブラリの例外/レスポンスで分岐）
                // subscriptionService.unsubscribe(s.getEndpoint());
            }
        }
        return delivered;
    }

    /** 表示/クリック遷移などで使うペイロード */
    public record PushPayload(
            String title,
            String body,
            String url,
            String icon,
            Long todoId,
            Long userId) {
    }
}
