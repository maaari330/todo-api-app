package com.example.todoapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TodoControllerTest {
    @Mock
    TodoRepository mockRepo; // (1) モック化されたリポジトリ

    @InjectMocks
    TodoController controller;  // (2) モックを注入して生成された Controller

    @Test
    void list_withoutKeyword_returnsAll() {
        // Arrange  // (3) テスト用データを準備
        var list = List.of(
            new Todo(1L, "A", false),
            new Todo(2L, "B", true)
        );

        // (4) モックの振る舞いを定義
        when(mockRepo.findAll()).thenReturn(list);

        // Act // (5) メソッド実行
        var result = controller.list(null);

        // Assert  // (6) 検証：戻り値の中身と、リポジトリ呼び出しをチェック
        assertThat(result).hasSize(2)
                          .extracting(Todo::getTitle)
                          .containsExactly("A", "B");
        verify(mockRepo).findAll();
    }

    @Test
    void list_withKeyword_callsFindByTitleContaining() {
        // Arrange
        String kw = "foo";
        var filtered = List.of(new Todo(3L, "foo-bar", false));
        when(mockRepo.findByTitleContaining(kw)).thenReturn(filtered);

        // Act
        var result = controller.list(kw);

        // Assert
        assertThat(result).hasSize(1)
                          .first()
                          .matches(t -> t.getTitle().contains("foo"));
        verify(mockRepo).findByTitleContaining(kw);
    }
}
