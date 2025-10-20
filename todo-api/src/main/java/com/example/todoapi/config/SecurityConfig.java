package com.example.todoapi.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.List;
import lombok.RequiredArgsConstructor;

import com.example.todoapi.security.JwtAuthenticationFilter;

/**
 * Spring Boot起動時にアプリケーション全体のセキュリティ設定として組み込まれ、
 * 各 HTTP リクエストで利用
 */
@Configuration
@EnableWebSecurity(debug = true)
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 「認証の中核ロジック」を起動時に Bean にする
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** CORS 設定を定義 */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // フロントの URLを設定。このURLからのリクエストだけがバックエンドへのアクセスを許可される
        cfg.setAllowedOrigins(List.of("http://localhost:3000"));
        // 必要なメソッドを許可
        cfg.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()));
        // 必要なヘッダーを許可
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        // Cookie（認証情報付きリクエスト）を許可
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 全パスに適用
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    // フィルターと各URLの設定を起動時に行い、以降すべての HTTP リクエストでそのチェーンを適用
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. ステートレスに
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 2. CSRF を 無効化
                .csrf(csrf -> csrf.disable())
                // 3. CORS を有効化
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 4. エンドポイント毎の認可設定
                .authorizeHttpRequests(authz -> authz
                        // ─── 認証不要 ─────────────────────────────
                        .requestMatchers("/auth/login", "/auth/signup").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // ─── 認証必須 ─────────────────────────────
                        .requestMatchers("/auth/logout").authenticated()
                        .requestMatchers("/auth/me", "/notifications/**", "/push/**").authenticated()
                        .requestMatchers("/actuator/**", "/todos/**", "/categories/**", "/tags/**").authenticated()
                        // ─── その他 ───────────────────────────────
                        .anyRequest().denyAll())
                // 4. Basic 認証は actuator のみで使う → 全体では無効化
                .httpBasic(httpBasic -> httpBasic.disable())
                // 5. JWT フィルターの有効性のみをチェック
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // 6. 5
        // のjwtAuthenticationFilterで設定されたAuthentication認証情報を上記のhttp.authorizeHttpRequestsや@PreAuthorizeで使用
        return http.build();
    }

    /** セキュリティのデバッグモードを有効化する設定。 フィルターチェーンの構築時・リクエストごとのフィルター実行順がログに出力される */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.debug(true);
    }
}