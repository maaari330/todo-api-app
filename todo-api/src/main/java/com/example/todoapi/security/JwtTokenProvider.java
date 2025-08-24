package com.example.todoapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import com.example.todoapi.config.JwtProperties;
import com.example.todoapi.entity.Role;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

// トークン生成・検証が目的
@Component

public class JwtTokenProvider {
    /** 生成 */
    // 秘密鍵（32文字以上推奨）
    private final Key secretKey;
    private final long expiration;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(JwtProperties properties, UserDetailsService userDetailsService) {
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecretKey().getBytes());
        this.expiration = properties.getExpiration();
        this.userDetailsService = userDetailsService;
    }
    // トークンを生成する
    public String generateToken(String username, Set<Role> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        // JWT ペイロードとして安全に扱うために enum を文字列に変換
        List<String> roleNames = roles.stream().map(Role::name).collect(Collectors.toList()); 
        claims.put("roles", roleNames);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setClaims(claims) // ユーザー名・ロールをセット
                .setIssuedAt(now)  // トークンの発行日時
                .setExpiration(expiry)  // 有効期限
                .signWith(secretKey, SignatureAlgorithm.HS256) // HMAC-SHA256 で署名
                .compact();  // コンパクト化
    }


    /** 検証 */
    // １．トークンからユーザー名を取得
    public String getUsername(String token) {
        return parseClaims(token).getSubject(); // トークン生成時のserSubjectでセットした値を取得
    }
    // ２．トークンからロールを取得
    public Set<String> getRoles(String token) {
        Claims claims = parseClaims(token); // クレーム（Claims）：JWTペイロード部に含まれる情報（認証情報など）
        List<?> rawRoles = claims.get("roles", List.class); // get(key, List.class) で List<?> として取り出す
        return rawRoles.stream() // 要素を String にキャストしながら Set に詰め直す
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toSet());
    }
    // ３．トークンが有効かどうか検証
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    // ４．トークンからAuthenticationへの変換（サーバー側の SecurityContext に保持するためjavaオブジェクト化）
    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username); // UserDetails からusernameをロードして Authenticationのprincipal にセット
        // Authentication オブジェクトを生成
        return new UsernamePasswordAuthenticationToken(
            userDetails,    // principal に UserDetails
            null,        // credentials は既に検証済みなので null
            userDetails.getAuthorities()  // 権限リスト
        );
    }
    // （共通処理：JWT の中身（クレーム）を取り出す）
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) // 署名検証に使う鍵を設定
                .build()
                .parseClaimsJws(token) // 署名検証とクレーム抽出
                .getBody();
    }
}
