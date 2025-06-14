
package com.example.todoapi;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description="TODO の一意なID", example="1")
    private Long id;   

    @Schema(description="TODO のタイトル", example="買い物に行く")
    private String title;

    @Schema(description="TODO の遂行状況フラグ", example="false")
    private boolean done;

    public Todo() { }  // デフォルトコンストラクタ
    public Todo(Long id, String title, Boolean done) {
        this.id = id;
        this.title = title;
        this.done = done;
    }

    // ゲッター・セッター
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
