package com.example.todoapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "ログイン成功時に返却するトークン情報") // レスポンス専用DTO
public class TokenResponse {
    @Schema(description = "JWT アクセストークン", example = "eyJhbGci…")
    private String accessToken;

    @Schema(description = "トークンタイプ（Bearer）", example = "Bearer")
    private String tokenType;
}