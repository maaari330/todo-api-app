package com.example.todoapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import com.example.todoapi.repository.PushSubscriptionRepository;

import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Notification;
import org.apache.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class WebPushSender {
    @Value("${notifications.webpush.subject:mailto:admin@example.com}")
    String subject;
    @Value("${notifications.webpush.publicKey:}")
    String publicKey;
    @Value("${notifications.webpush.privateKey:}")
    String privateKey;
    private final PushSubscriptionRepository repo;

    public void sendToUser(Long userId, String title, String body, String url) {
        if (publicKey.isBlank() || privateKey.isBlank())
            return; // 未設定ならスキップ
        try {
            PushService svc = new PushService(publicKey, privateKey, subject);
            for (var s : repo.findByUserId(userId)) {
                String payload = """
                        {"title":"%s","body":"%s","url":"%s"}
                        """.formatted(escape(title), escape(body), escape(url));
                Notification n = new Notification(s.getEndpoint(), s.getP256dh(), s.getAuth(), payload);
                try {
                    svc.send(n);
                } catch (Exception e) {
                    /* endpoint失効などは無視 */ }
            }
        } catch (Exception e) {
            // ログに留める
        }
    }

    private String escape(String v) {
        return v.replace("\"", "\\\"");
    }
}
