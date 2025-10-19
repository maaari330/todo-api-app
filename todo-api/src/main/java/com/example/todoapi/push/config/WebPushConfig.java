package com.example.todoapi.push.config;

import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.PushService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.lang.reflect.Constructor;

import java.util.Base64;
import java.security.KeyPair;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Configuration
@RequiredArgsConstructor
public class WebPushConfig {
  private final VapidProperties vapid;

  @Bean
  public PushService pushService() {
    String pub = vapid.getPublicKey();
    String prv = vapid.getPrivateKey();
    String subject = vapid.getSubject();

    // まずは (String, String, String) コンストラクタを試す
    try {
      Constructor<PushService> c1 = PushService.class.getDeclaredConstructor(String.class, String.class, String.class);
      c1.setAccessible(true);
      return c1.newInstance(pub, prv, subject);
    } catch (NoSuchMethodException ignored) {
      // フォールバックへ
    } catch (Exception e) {
      throw new RuntimeException("Failed to init PushService (public/private/subject ctor)", e);
    }

    // フォールバック: KeyPair を構築して (KeyPair, String) を試す
    try {
      KeyPair keyPair = buildKeyPairFromBase64Url(pub, prv);
      Constructor<PushService> c2 = PushService.class.getDeclaredConstructor(KeyPair.class, String.class);
      c2.setAccessible(true);
      return c2.newInstance(keyPair, subject);
    } catch (Exception e) {
      throw new RuntimeException("Failed to init PushService (KeyPair/subject ctor)", e);
    }
  }

  /**
   * Base64URL(=無し) から鍵を再構築。
   * 期待形式はライブラリにより X509/SPKI（公開）・PKCS8（秘密）が一般的。
   */
  private static KeyPair buildKeyPairFromBase64Url(String publicKeyB64Url, String privateKeyB64Url) throws Exception {
    byte[] pubDer = Base64.getUrlDecoder().decode(publicKeyB64Url);
    byte[] prvDer = Base64.getUrlDecoder().decode(privateKeyB64Url);

    KeyFactory kf = KeyFactory.getInstance("EC");
    PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(pubDer));
    PrivateKey prv = kf.generatePrivate(new PKCS8EncodedKeySpec(prvDer));
    return new KeyPair(pub, prv);
  }
}