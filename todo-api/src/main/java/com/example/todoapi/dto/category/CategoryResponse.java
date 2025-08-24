package com.example.todoapi.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.example.todoapi.entity.Category;

@Getter
@AllArgsConstructor
@Schema(description = "カテゴリレスポンス")
public class CategoryResponse {
  @Schema(description = "カテゴリID", example = "1")
  private final Long id;

  @Schema(description = "カテゴリ名", example = "仕事")
  private final String name;

  public static CategoryResponse from(Category c) {
    return new CategoryResponse(c.getId(), c.getName());
  }
}