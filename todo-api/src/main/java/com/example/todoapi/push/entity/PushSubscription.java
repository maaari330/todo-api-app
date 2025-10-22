package com.example.todoapi.push.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "push_subscriptions", uniqueConstraints = @UniqueConstraint(name = "uq_user_endpoint", columnNames = {
        "user_id", "endpoint" }))
@Getter
@Setter
@NoArgsConstructor
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** アプリ上のユーザーID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** FCM 等のエンドポイント */
    @Column(nullable = false, length = 500)
    private String endpoint;

    /** Base64URL（ブラウザから来た p256dh をそのまま） */
    @Column(nullable = false, length = 150)
    private String p256dh;

    /** Base64URL（ブラウザから来た auth をそのまま） */
    @Column(nullable = false, length = 150)
    private String auth;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}