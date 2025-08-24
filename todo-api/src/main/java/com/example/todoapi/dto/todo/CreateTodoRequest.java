package com.example.todoapi.dto.todo;

import java.time.LocalDateTime;

import com.example.todoapi.entity.RepeatType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
@Schema(description = "タスク新規作成時に送信されるリクエストボディのフォーマット")
public class CreateTodoRequest {
    @NotBlank(message = "タイトルを入力してください")
    @Schema(description = "TODO のタイトル", example = "買い物に行く")
    private String title;

    @Schema(description = "TODO の完了フラグ", example = "false", defaultValue = "false")
    private Boolean done = false;

    @Schema(description = "TODO の期限 (秒付き ISO-8601)", example = "2025-07-18T19:55:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    @Schema(description = "繰り返し種別 (NONE, DAILY, WEEKLY, MONTHLY)", example = "WEEKLY")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private RepeatType repeatType = RepeatType.NONE;

    @Schema(description = "紐付けるカテゴリのID", example = "1")
    private Long categoryId;

    @Schema(description = "紐付けるタグのID一覧", example = "[1,2,3]")
    private Set<Long> tagIds;
}
