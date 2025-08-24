// CreateTagRequest.java
package com.example.todoapi.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "新規タグ作成リクエスト")
public class CreateTagRequest {
    @NotBlank
    @Schema(description = "タグ名", example = "Urgent")
    private String name;
}