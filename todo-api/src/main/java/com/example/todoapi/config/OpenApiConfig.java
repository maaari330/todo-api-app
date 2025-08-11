package com.example.todoapi.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;

/** Swagger UI の「Authorize」ダイアログに Bearer JWT 認証入力欄を出すための設定クラス
 * 貼り付けた JWT がすべてのリクエストの Authorization: Bearer <token> ヘッダーに乗る
 */
@OpenAPIDefinition( security = { @SecurityRequirement(name = "bearerAuth") } )
@Configuration
@SecurityScheme(
   name         = "bearerAuth",  // 任意の識別子
   type         = SecuritySchemeType.HTTP, // HTTP 認証タイプ
   scheme       = "bearer", // Bearer トークン
   bearerFormat = "JWT", // フォーマットは JWT
   in           = SecuritySchemeIn.HEADER // Authorization ヘッダに乗せる
)
public class OpenApiConfig {
    // 空クラスで OK。アノテーションだけで機能する
}