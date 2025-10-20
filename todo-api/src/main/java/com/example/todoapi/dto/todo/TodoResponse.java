package com.example.todoapi.dto.todo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.todoapi.entity.Todo;
import com.example.todoapi.entity.RepeatType;
import com.example.todoapi.entity.Tag;

@Getter
@AllArgsConstructor
@Schema(description = "TODO レスポンス")
public class TodoResponse {
  @Schema(description = "ID", example = "1")
  private final Long id;

  @Schema(description = "タイトル", example = "買い物")
  private final String title;

  @Schema(description = "完了フラグ", example = "false")
  private final Boolean done;

  @Schema(description = "期限日", example = "2025-08-01")
  private final LocalDateTime dueDate;

  @Schema(description = "繰り返し種別", example = "DAILY")
  private final RepeatType repeatType;

  @Schema(description = "期限の何分前に通知", example = "60")
  private final Integer remindOffsetMinutes;

  @Schema(description = "オーナーID", example = "5")
  private final Long ownerId;

  @Schema(description = "カテゴリID", example = "2")
  private final Long categoryId;

  @Schema(description = "タグID一覧", example = "[1,3]")
  private final Set<Long> tagIds;

  public static TodoResponse from(Todo todo) {
    return new TodoResponse(
        todo.getId(),
        todo.getTitle(),
        todo.isDone(),
        todo.getDueDate(),
        todo.getRepeatType() != null ? todo.getRepeatType() : RepeatType.NONE,
        todo.getRemindOffsetMinutes(),
        todo.getOwner() != null ? todo.getOwner().getId() : null,
        todo.getCategory() != null ? todo.getCategory().getId() : null,
        todo.getTags().stream().map(Tag::getId).collect(Collectors.toSet()));
  }
}