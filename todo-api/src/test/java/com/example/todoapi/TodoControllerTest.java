package com.example.todoapi;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TodoControllerTest {
    @InjectMocks
    TodoController controller;  // (2) モックを注入して生成された Controller

    @Mock
    TodoService mockService; 

    @Test
    void list_withoutKeyword_returnsAll() {
        // Arrange  // (3) テスト用データを準備
        var list = List.of(
            new Todo(1L, "A", false),
            new Todo(2L, "B", true)
        );

        // (4) モックの振る舞いを定義
        when(mockService.list(null)).thenReturn(list);

        // Act // (5) メソッド実行
        var result = controller.list(null);

        // Assert  // (6) 検証：戻り値の中身と、service呼び出しをチェック
        assertThat(result).hasSize(2)
                          .extracting(Todo::getTitle)
                          .containsExactly("A", "B");
        verify(mockService).list(null);
    }

    @Test
    void list_withKeyword_callsFindByTitleContaining() {
        // Arrange
        String kw = "foo";
        var filtered = List.of(new Todo(3L, "foo-bar", false));
        when(mockService.list(kw)).thenReturn(filtered);

        // Act
        var result = controller.list(kw);

        // Assert
        assertThat(result).hasSize(1)
                          .first()
                          .matches(t -> t.getTitle().contains("foo"));
        verify(mockService).list(kw);
    }

    @Test
    void update_existingId_returnsUpdatedTodo() {
        // Arrange
        Long id = 1L;
        UpdateTodoRequest req = new UpdateTodoRequest();
        req.setTitle("更新後タイトル");
        Todo updated = new Todo(id, "更新後タイトル", false);

        // サービスの振る舞い定義
        when(mockService.update(id, req)).thenReturn(updated);

        // Act
        ResponseEntity<Todo> response = controller.update(id, req);

        // Assert ステータス
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
         // ↓ ここで null チェックと例外スローを同時に行う
        Todo body = Objects.requireNonNull(
        response.getBody(),
        "Response body must not be null"
        );

        // 以降は body が null でないと保証される
        assertThat(body.getTitle()).isEqualTo("更新後タイトル");
    }

   @Test
    void update_notFoundId_throwsNotFound() {
        // Arrange
        Long id = 999L;
        UpdateTodoRequest req = new UpdateTodoRequest();
        req.setTitle("何か");

        // 存在しない ID の場合、サービス層で 404 を投げる
        when(mockService.update(id, req))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThatThrownBy(() -> controller.update(id, req))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(ex -> {
            var rse = (ResponseStatusException) ex;
            assertThat(rse.getStatusCode().value()).isEqualTo(404);
        });

        verify(mockService).update(id, req);
    } 

    @Test
    void delete_existingId_returns204() {
        Long id = 1L;
        
        // サービス側の delete メソッドは void を想定
        doNothing().when(mockService).delete(id);
        
        // 実行
        ResponseEntity<Void> response = controller.delete(id);
        
        // 検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(mockService).delete(id);
    }

    @Test
    void toggleDone_existingId_returnsUpdatedTodo() {
        Long id = 2L;
        // もともと done=false だったときに true になって返る想定
        Todo toggled = new Todo(id, "タイトル", true);
        when(mockService.toggleDone(id)).thenReturn(toggled);
        
        ResponseEntity<Todo> response = controller.toggleDone(id);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK).isNotNull();
        // 1) レスポンスボディをローカル変数に取り出す
        Todo body = Objects.requireNonNull(response.getBody());
        // 2) boolean プロパティを取り出してアサート
        assertThat(body.isDone()).isTrue();
        verify(mockService).toggleDone(id);
    }
}
