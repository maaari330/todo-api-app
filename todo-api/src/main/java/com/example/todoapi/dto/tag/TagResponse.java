package com.example.todoapi.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.example.todoapi.entity.Tag;

@Getter
@AllArgsConstructor
@Schema(description = "タグレスポンス")
public class TagResponse {
    @Schema(description = "タグID", example = "1")
    private final Long id;

    @Schema(description = "タグ名", example = "Urgent")
    private final String name;

    public static TagResponse from(Tag t) {
        return new TagResponse(t.getId(), t.getName());
    }
}