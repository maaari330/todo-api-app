package com.example.todoapi.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/** 通知ジョブの間隔などの設定を application.yml / 環境変数から自動でこのクラスのフィールドへ読み込むクラス */
@Getter
@Setter
@ConfigurationProperties(prefix = "notify")
public class NotifyProperties {
    private long scanMs = 60000; // ミリ秒
}