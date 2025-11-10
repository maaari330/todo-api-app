package com.example.todoapi.push.config;

import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.PushService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ★ 追加：BouncyCastle を登録する
import java.security.Security;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

@Configuration
@RequiredArgsConstructor
public class WebPushConfig {

  private final VapidProperties vapid;

  @Bean
  public PushService pushService() {
    try {
      // ★ BC プロバイダを一度だけ登録（ない場合）
      if (Security.getProvider("BC") == null) {
        Security.addProvider(new BouncyCastleProvider());
      }

      String publicKeyBase64  = toStandardBase64(vapid.getPublicKey());
      String privateKeyBase64 = toStandardBase64(vapid.getPrivateKey());

      // ★ 反射や KeyPair 生成は不要。Base64URL の公開鍵/秘密鍵と subject をそのまま渡す
      return new PushService(
          vapid.getPublicKey(),
          vapid.getPrivateKey(),
          vapid.getSubject());
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to init PushService. Check VAPID_PUBLIC_KEY / VAPID_PRIVATE_KEY / VAPID_SUBJECT.",
          e);
    }
  }

  private static String toStandardBase64(String key){
    if (key == null || key.isBlank()) return key;

    key = key.trim();
    if (key.startsWith("\"") && key.endsWith("\"") && key.length() >= 2) {
      key = key.substring(1, key.length() - 1);
    }
    try {
      int padding = (4 - (key.length() % 4)) % 4;
      String padded = key + "=".repeat(padding);
      byte[] decoded = Base64.getUrlDecoder().decode(padded);   // URL-safe としてデコード
      return Base64.getEncoder().encodeToString(decoded);        // 標準 Base64 に再エンコード
    } catch (IllegalArgumentException e) {
      // すでに標準 Base64 だった場合はそのまま返す
      return key;
    }
  }
}