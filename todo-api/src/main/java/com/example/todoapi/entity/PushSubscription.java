package com.example.todoapi.entity;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "push_subscriptions")
@Schema(description = "Web Push通知の購読情報（endpoint と鍵 p256dh/auth）をユーザーに紐づけて保持するエンティティ。1ユーザーが複数の端末・ブラウザを持つ想定で、リマインド通知送信時の配信先として参照する。")
public class PushSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 1024)
    private String endpoint;

    @Column(nullable = false)
    private String p256dh;

    @Column(nullable = false)
    private String auth;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
