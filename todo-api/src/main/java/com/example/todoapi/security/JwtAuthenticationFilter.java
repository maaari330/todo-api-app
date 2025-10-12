package com.example.todoapi.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

import org.springframework.lang.NonNull;
import org.springframework.http.HttpHeaders;

/**
 * 毎リクエストでSpring Security のフィルターとして JWT の検証と
 * Spring Security（Security Config） に認証情報を渡す「フィルター」
 */
@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // OncePerRequestFilterでは shouldNotFilter() →
                                                                    // doFilterInternal()の順で呼ぶ
    private final JwtTokenProvider tokenProvider;

    // これらのパスでは doFilterInternal をスキップする
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return "/auth/signup".equals(path)
                || "/auth/login".equals(path) // login成功して初めてJWTトークンが発行されるため、トークン検証（doFilterInternal）は対象外
                || "/error".equals(path);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
            @NonNull FilterChain chain) // HttpServletRequest:クライアントから送られてきた HTTP リクエスト情報を表現
            throws ServletException, IOException {
        // 1) Authorization ヘッダーを優先して取得
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        String token = (header != null && header.startsWith("Bearer "))
                ? header.substring(7)
                : null;

        /**
         * 2) トークンがあれば検証し、問題なければ認証情報をセット
         * SecurityContext に登録 → コントローラ／サービス層で @AuthenticationPrincipal UserDetails
         * によって利用
         */
        if (token != null) {
            try {
                if (tokenProvider.validateToken(token)) {
                    Authentication auth = tokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                // トークンが無効、またはユーザー不在なら例外をキャッチ
            } catch (JwtException | UsernameNotFoundException ex) {
                logger.debug("Invalid JWT, skipping authentication: " + ex.getMessage());
                SecurityContextHolder.clearContext(); // SecurityContext はクリア（＝未認証状態のまま）→ 未認証ユーザー扱い
            }
        }
        chain.doFilter(req, res); // 次のフィルタに処理を渡す
    }
}
