package com.example.todoapi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTodoRequest {
    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 255, message = "タイトルは255文字以内で入力してください")
    private String title;

    // 部分更新で done も受け取りたい
    private Boolean done;
}
