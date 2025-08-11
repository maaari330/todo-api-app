package com.example.todoapi.dto;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "エラー発生時に返却される標準フォーマット")
public class ErrorResponse {
    @Schema(description = "エラー発生時刻 (秒付き ISO-8601)", example = "2025-07-01T14:23:45")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP ステータスコード", example = "400")
    private int status;

    @Schema(description = "エラー名 (Reason Phrase)", example = "Bad Request")
    private String error;

    @Schema(description = "詳細メッセージ", example = "title: must not be blank; done: must not be null")
    private String message;

    @Schema(description = "リクエストパス", example = "/todos")
    private String path;
}
