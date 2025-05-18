package com.example.todoapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TodoRepositoryTest {

    @Autowired
    TodoRepository repo;   // H2 組み込み DB が自動設定されます

    @Test
    void findByTitleContaining_shouldReturnMatching() {
        // Arrange: テスト用データを保存
        repo.save(new Todo(null, "日本語テスト", false));
        repo.save(new Todo(null, "another", true));

        // Act: 「テスト」を含むものを検索
        List<Todo> result = repo.findByTitleContaining("テスト");

        // Assert: 正しく 1 件返ってくるか
        assertThat(result)
          .hasSize(1)
          .first()
          .extracting(Todo::getTitle)
          .isEqualTo("日本語テスト");
    }
}