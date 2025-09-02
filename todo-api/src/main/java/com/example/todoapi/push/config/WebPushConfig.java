package com.example.todoapi.push.config;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;

import java.security.Security;

/** PushService のBean初期化（VAPID公開鍵/秘密鍵/subjectを設定） */
@Configuration
@RequiredArgsConstructor
public class WebPushConfig {
  private final VapidProperties vapid; // VapidPropertiesクラスから値を取得

  @Bean // 起動時に公開鍵/秘密鍵/subjectをセットする
  public PushService pushService() throws Exception {
    Security.addProvider(new BouncyCastleProvider()); // BouncyCastle：鍵作り・暗号化実装可能にするための“追加プラグイン”
    PushService svc = new PushService();
    svc.setPublicKey(vapid.getPublicKey());
    svc.setPrivateKey(vapid.getPrivateKey());
    svc.setSubject(vapid.getSubject());
    return svc;
  }
}
