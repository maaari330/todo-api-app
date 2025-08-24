package com.example.todoapi.dto.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

import com.example.todoapi.entity.RepeatType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "タスク更新時に送信されるリクエストボディのフォーマット")
public class UpdateTodoRequest {
    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 255, message = "タイトルは255文字以内で入力してください")
    @Schema(description = "TODO のタイトル", example = "掃除をする")
    private String title;

    @Schema(description = "TODO の完了フラグ", example = "true")
    private Boolean done;

    @Schema(description = "TODO の期限")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    @Schema(description = "繰り返し種別 (NONE, DAILY, WEEKLY, MONTHLY)", example = "DAILY")
    private RepeatType repeatType = RepeatType.NONE;

    @Schema(description = "紐付けるカテゴリのID", example = "1")
    private Long categoryId;

    @Schema(description = "紐付けるタグのID一覧", example = "[1,2,3]")
    private Set<Long> tagIds;
}
