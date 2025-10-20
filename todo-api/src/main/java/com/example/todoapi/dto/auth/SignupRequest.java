package com.example.todoapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Pattern;

//新しいユーザーを登録するため、バリデーションを厳しくする
@Getter
@Setter
@Schema(description = "サインアップリクエストDTO") 
public class SignupRequest {
    @Schema(description = "ユーザー名（4～20文字）", example = "taro123")
    @NotBlank
    @Size(min = 4, max = 20)
    private String username;

    @Schema(description = "パスワード（英大小文字・数字を含む8〜50文字）", example = "Abc12345")
    @NotBlank
    @Size(min = 6, max = 50)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "パスワードは英大文字・小文字・数字を1つ以上含める必要があります")
    private String password;
}
