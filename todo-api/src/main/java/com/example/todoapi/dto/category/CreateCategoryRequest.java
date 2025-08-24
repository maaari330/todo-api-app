package com.example.todoapi.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "カテゴリ作成リクエスト")
public class CreateCategoryRequest {
  
  @NotBlank
  @Schema(description = "カテゴリ名", example = "仕事", required = true)
  private String name;
}