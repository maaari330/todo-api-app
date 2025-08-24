package com.example.todoapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// 登録済ユーザーの本人確認（認証）をするため、最小限でよい
@Getter
@Setter
@Schema(description = "ログインリクエストDTO")
public class LoginRequest {

    @Schema(description = "ユーザー名", example = "testuser")
    @NotBlank(message = "ユーザー名は必須です")
    private String username;

    @Schema(description = "パスワード", example = "test1234")
    @NotBlank(message = "パスワードは必須です")
    private String password;
}

