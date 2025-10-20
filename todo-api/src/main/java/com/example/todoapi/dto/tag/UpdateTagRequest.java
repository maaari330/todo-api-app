package com.example.todoapi.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "タグ更新リクエスト")
public class UpdateTagRequest {
    @NotBlank
    @Schema(description = "新しいタグ名", example = "High Priority")
    private String name;
}