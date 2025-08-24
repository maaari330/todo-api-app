package com.example.todoapi.dto.auth;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/** 認証済みユーザー情報返却用 DTO */
@Schema(description = "認証に成功したユーザーの基本情報を返却するDTO")
public record UserDto (
    @Schema(description = "ユーザーID", example = "1")
    Long id,

    @Schema(description = "ユーザー名", example = "exampleUser")
    String username,

    @Schema(description = "ユーザーに付与されているロール一覧", example = "[\"ROLE_USER\",\"ROLE_ADMIN\"]")
    List<String> roles
) {}
