package com.example.todoapi.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "カテゴリ更新リクエスト")
public class UpdateCategoryRequest {

  @NotBlank
  @Schema(description = "カテゴリ名", example = "プライベート", required = true)
  private String name;
}