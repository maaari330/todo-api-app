package com.example.todoapi.controller;

import com.example.todoapi.dto.auth.LoginRequest;
import com.example.todoapi.dto.auth.SignupRequest;
import com.example.todoapi.dto.auth.TokenResponse;
import com.example.todoapi.service.AuthService;
import com.example.todoapi.dto.auth.UserDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "User Authentication API", description = "ユーザーのサインアップ、ログイン、ログアウト、および認証状態確認")
@CrossOrigin(origins = "http://localhost:3000") 
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "ユーザー新規登録")
    @ApiResponse(responseCode = "201", description = "ユーザー登録成功")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "ログイン", description = "ユーザー名とパスワードを検証し、JWT トークンを返却")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "ログイン成功"), @ApiResponse(responseCode = "401", description = "認証失敗") })
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        String token = authService.login(request);
        // JSON ボディにも同じトークンを返却（SwaggerUIのAuthorizeダイアログがaccessTokenを使用可に）
        TokenResponse body = new TokenResponse(token, "Bearer");
        return ResponseEntity.ok().body(body);
    }

    @PostMapping("/logout")
    @Operation(summary = "ログアウト", description = "クライアント側でトークンを破棄してください")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description  = "ログアウト成功。accessToken Cookie を削除しました。"),
        @ApiResponse(responseCode = "401", description  = "未認証。ログイン状態ではありません。"),
        @ApiResponse(responseCode = "500", description  = "サーバー内部エラーが発生しました。")
    })
    public ResponseEntity<Void> logout(HttpServletRequest request) {
         // サーバ側では特に何もしない（クライアントがトークンを破棄する）
        return ResponseEntity.ok().build();
    }

    /** 認証状態チェック+ユーザー情報取得用エンドポイント
     * SecurityConfigのフィルター群により検証 */ 
    @GetMapping("/me")
    @Operation(summary = "認証状態の検証", description = "Bearer トークンを検証し、有効なら 200 OK を返します。")
    @ApiResponses({
      @ApiResponse(responseCode = "200", description = "認証済み（トークン有効）"),
      @ApiResponse(responseCode = "401", description = "未認証またはトークン無効")
    })
    public ResponseEntity<UserDto> me(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserDto dto = authService.me(userDetails);
        return ResponseEntity.ok(dto);
    }
}
