package com.example.todoapi.push.config;

import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import java.security.KeyPair;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class WebPushConfig {
  private final VapidProperties vapid;

  private static byte[] decodeBase64Url(String s) {
    String fixed = s.trim().replace('-', '+').replace('_', '/');
    int pad = (4 - (fixed.length() % 4)) % 4;
    if (pad != 0)
      fixed = fixed + "====".substring(0, pad);
    return Base64.getDecoder().decode(fixed);
  }

  @Bean
  public PushService pushService() throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    // 1) Base64URL → bytes に正規化してから鍵生成
    byte[] pubBytes = decodeBase64Url(vapid.getPublicKey());
    byte[] privBytes = decodeBase64Url(vapid.getPrivateKey());

    var pub = Utils.loadPublicKey(pubBytes); // ★ byte[] 版を使う
    var priv = Utils.loadPrivateKey(privBytes);

    // 2) 起動時検証（壊れ鍵なら即エラー）
    if (!(pub instanceof ECPublicKey p) || !(priv instanceof ECPrivateKey q)) {
      throw new IllegalArgumentException("VAPID keys are not EC P-256");
    }
    // 公開鍵は未圧縮ポイント 65バイト（0x04 + X(32) + Y(32)）が一般的
    if (pubBytes.length != 65 && pubBytes.length != 91) { // 実装差の吸収で91も許容
      System.err.printf("[vapid] unusual public key size: %d bytes%n", pubBytes.length);
    }
    if (privBytes.length != 32 && privBytes.length != 48) {
      System.err.printf("[vapid] unusual private key size: %d bytes%n", privBytes.length);
    }

    var kp = new KeyPair(pub, priv);

    // 3) PushService に“鍵ペア”で渡す（Crypto-Key p256ecdsa=... を正しく組み立てる）
    var svc = new PushService();
    svc.setKeyPair(kp);
    svc.setSubject(vapid.getSubject()); // mailto: または https://
    return svc;
  }

  @PostConstruct
  public void logRuntimeDeps() {
    System.out.println("[diag] PushService.jar = " +
        nl.martijndwars.webpush.PushService.class
            .getProtectionDomain().getCodeSource().getLocation().getPath());
    System.out.println("[diag] Utils.jar       = " +
        nl.martijndwars.webpush.Utils.class
            .getProtectionDomain().getCodeSource().getLocation().getPath());
    System.out.println("[diag] BC Provider     = " +
        java.security.Security.getProviders()[0]);
  }
}