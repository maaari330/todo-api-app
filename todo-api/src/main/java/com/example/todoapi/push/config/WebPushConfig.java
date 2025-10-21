package com.example.todoapi.push.config;

import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.PushService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ★ 追加：BouncyCastle を登録する
import java.security.Security;
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
}