package com.example.todoapi.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tag")
@Schema(description = "タグエンティティ")
public class Tag {
    /** JPA が使う引数なしコンストラクタ */
    protected Tag() { }

    /** アプリ側が使う全属性コンストラクタ */
    public Tag(Long id, String name) {
        this.id   = id;
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}