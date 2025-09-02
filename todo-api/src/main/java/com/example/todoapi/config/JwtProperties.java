package com.example.todoapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

// JWTのシークレットや有効期限を application.yml / 環境変数から自動でこのクラスのフィールドへ読み込むクラス
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretKey;
    private long expiration;
}
