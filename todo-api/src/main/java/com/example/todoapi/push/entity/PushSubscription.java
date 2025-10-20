package com.example.todoapi.push.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "push_subscriptions", indexes = {
        @Index(name = "idx_push_endpoint", columnList = "endpoint", unique = true),
        @Index(name = "idx_push_owner", columnList = "owner_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** アプリ上のユーザーID（未ログイン購読なら null でもOK） */
    @Column(name = "owner_id")
    private Long ownerId;

    /** FCM 等のエンドポイント */
    @Column(nullable = false, length = 512)
    private String endpoint;

    /** Base64URL（ブラウザから来た p256dh をそのまま） */
    @Column(nullable = false, length = 255)
    private String p256dh;

    /** Base64URL（ブラウザから来た auth をそのまま） */
    @Column(nullable = false, length = 255)
    private String auth;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = OffsetDateTime.now();
    }
}