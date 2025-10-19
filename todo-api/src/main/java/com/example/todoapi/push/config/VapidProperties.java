package com.example.todoapi.push.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/** Web Push（VAPID）の設定を application.yml / 環境変数から自動でこのクラスのフィールドへ読み込むクラス */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vapid")
public class VapidProperties {
    private String publicKey;
    private String privateKey;
    private String subject;
}
