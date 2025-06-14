package com.example.todoapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(authz -> authz
              // ヘルスとインフォは匿名アクセス OK
              .requestMatchers("/actuator/health", "/actuator/info").permitAll()
              // それ以外の actuator は認証が必要
              .requestMatchers("/actuator/**").authenticated()
              // 他の API はこれまで通り
              .anyRequest().permitAll()
          )
          .httpBasic(Customizer.withDefaults())                  // Basic 認証
           .csrf(csrf -> csrf
            .ignoringRequestMatchers(
            "/actuator/**",
            "/todos/**"         // ← /todos/* の POST/PUT/DELETE も CSRF 対象外に
            )
           );
        return http.build();
    }
}