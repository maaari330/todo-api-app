package com.example.todoapi;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.example.todoapi.dto.todo.CreateTodoRequest;
import com.example.todoapi.dto.todo.UpdateTodoRequest;
import com.example.todoapi.service.TodoService;
import com.example.todoapi.controller.TodoController;
import com.example.todoapi.entity.Todo; 

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TodoControllerTest {
    @InjectMocks
    TodoController controller;  // モックを注入して生成された Controller

    @Mock
    TodoService mockService; 

    @Test
    void list_returnsAllPage() {
        // Arrange  テスト用データを準備
        var list = List.of(
            new Todo(1L, "A", false, LocalDateTime.of(2025, 6, 25, 18, 0)),
            new Todo(2L, "B", true, null)
        );
        Page<Todo> page = new PageImpl<>(list);
        when(mockService.list(null, null, Pageable.unpaged())).thenReturn(page);
        // Act メソッド実行
        Page<Todo> result = controller.list(null, null, Pageable.unpaged());
        // Assert  // 検証：戻り値の中身と、service呼び出しの２つをチェック
        assertThat(result).hasSize(2)
                          .extracting(Todo::getTitle)
                          .containsExactly("A", "B");
        verify(mockService).list(null, null, Pageable.unpaged());
    }

    @Test
    void list_withKeywordOnly_returnsFilteredPage() {
        // Arrange
        String kw = "foo";
        var filtered = List.of(new Todo(3L, "foo-bar", false, LocalDateTime.of(2025, 12, 31, 18, 0)));
        Page<Todo> page = new PageImpl<>(filtered);
        when(mockService.list(eq(kw), eq(null), any(Pageable.class))).thenReturn(page);
        // Act
        Page<Todo> result = controller.list(kw, null, Pageable.unpaged());
        // Assert
        assertThat(result).hasSize(1)
                          .first()
                          .matches(t -> t.getTitle().contains("foo"));
        verify(mockService).list(eq(kw), eq(null), any(Pageable.class));
    }

    @Test
    void list_withStatusOnly_returnsFilteredPage() {
        Boolean completed = true;
        var filtered = List.of(new Todo(4L, "test4", true, null));
        Page<Todo> page = new PageImpl<>(filtered);
        when(mockService.list(eq(null), eq(completed), any(Pageable.class))).thenReturn(page);
        // Act
        Page<Todo> result = controller.list(null, completed, Pageable.unpaged());
        // Assert
        assertThat(result)
          .hasSize(1)
          .first()
          .matches(t -> t.isDone());  // 完了フラグが true
        verify(mockService).list(eq(null), eq(completed), any(Pageable.class));
    }

    @Test
    void list_withKeywordAndStatus_returnsFilteredPage() {
        String kw = "bar";
        Boolean completed = false;
        var filtered = List.of(new Todo(5L, "bar-baz", false, null));
        Page<Todo> page = new PageImpl<>(filtered);
        when(mockService.list(eq(kw), eq(completed), any(Pageable.class))).thenReturn(page);
        // Act
        Page<Todo> result = controller.list(kw, completed, Pageable.unpaged());
        // Assert
        assertThat(result)
          .hasSize(1)
          .first()
          .matches(t ->
            t.getTitle().contains("bar") &&
            !t.isDone()                  // 完了フラグが false
          );
        verify(mockService).list(eq(kw), eq(completed), any(Pageable.class));
    }
    
    @Test
    void create_validRequest_returns201Created() {
        // Arrange
        CreateTodoRequest req = new CreateTodoRequest();
        req.setTitle("新規タスク");
        req.setDone(false);
        req.setDueDate(LocalDateTime.of(2025, 7, 1, 12, 0));
        Todo created = new Todo(10L, "新規タスク", false, LocalDateTime.of(2025, 7, 1, 12, 0));
        when(mockService.create(eq(req))).thenReturn(created);
        // Act
        ResponseEntity<Todo> response = controller.create(req);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Todo body = Objects.requireNonNull(response.getBody(), "Response body must not be null");
        assertThat(body.getId()).isEqualTo(10L);
        assertThat(body.getTitle()).isEqualTo("新規タスク");
        assertThat(body.isDone()).isFalse();
        assertThat(body.getDueDate()).isEqualTo(LocalDateTime.of(2025, 7, 1, 12, 0));
        verify(mockService).create(eq(req));
    }

    @Test
    void create_serviceThrowsBadRequest_propagatesException() {
        // Arrange
        CreateTodoRequest req = new CreateTodoRequest();
        // 不正なリクエストとしてサービス層が 400 を投げる想定
        when(mockService.create(eq(req)))
            .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input"));
        // Act & Assert
        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> controller.create(req)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Invalid input", ex.getReason());
        verify(mockService).create(eq(req));
    }

    @Test
    void update_existingId_returnsUpdatedTodo() {
        // Arrange
        UpdateTodoRequest req = new UpdateTodoRequest();
        req.setTitle("更新後タイトル");
        req.setDueDate(LocalDateTime.of(2025, 12, 31, 18, 0));
        Todo updated = new Todo(1L, "更新後タイトル", false, LocalDateTime.of(2025, 12, 31, 18, 0));
        when(mockService.update(1L, req)).thenReturn(updated);
        // Act
        ResponseEntity<Todo> response = controller.update(1L, req);
        // Assert updateに成功すると204(NO_CONTENT)ステータス
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(mockService).update(1L, req);
    }

   @Test
    void update_notFoundId_throwsNotFound404() {
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
    void update_throwsBadRequest400() {
        // Arrange
        UpdateTodoRequest req = new UpdateTodoRequest();
        req.setTitle(null);
        // title が null の場合、サービス層で 400 を投げる
        when(mockService.update(1L, req))
        .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        // Act & Assert
        assertThatThrownBy(() -> controller.update(1L, req))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(ex -> {
            var rse = (ResponseStatusException) ex;
            assertThat(rse.getStatusCode().value()).isEqualTo(400);
        });
        verify(mockService).update(1L, req);
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
        Todo toggled = new Todo(id, "タイトル", true, null);
        when(mockService.toggleDone(id)).thenReturn(toggled);
        // Act
        ResponseEntity<Todo> response = controller.toggleDone(id);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK).isNotNull();
        // 1) レスポンスボディをローカル変数に取り出す
        Todo body = Objects.requireNonNull(response.getBody());
        // 2) boolean プロパティを取り出してアサート
        assertThat(body.isDone()).isTrue();
        verify(mockService).toggleDone(id);
    }

    @Test
    void list_withPagingAndSort_passesCorrectPageable() {
        // Arrange
        String kw = "x";
        Boolean done = false;
        // Prepare a non-empty page to verify content as well
        Todo sampleTodo = new Todo(1L, "x", false, null);
        Page<Todo> page = new PageImpl<>(List.of(sampleTodo));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(mockService.list(eq(kw), eq(done), captor.capture())).thenReturn(page);
        // Act
        Pageable requested = PageRequest.of(2, 5, Sort.by(Sort.Direction.DESC, "dueDate"));
        Page<Todo> result = controller.list(kw, done, requested);
        // Assert pageable parameters コントローラ層からサービス層に正しいパラメータが渡されたか確認
        Pageable passed = captor.getValue();
        assertThat(passed.getPageNumber()).isEqualTo(2);
        assertThat(passed.getPageSize()).isEqualTo(5);
        Sort.Order order = Objects.requireNonNull(passed.getSort().getOrderFor("dueDate"), "Sort order for dueDate must not be null");
        assertThat(order).withFailMessage("dueDate のソート情報が渡されていません").isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
        // Assert returned content
        assertThat(result.getContent()).hasSize(1)
            .first()
            .matches(t -> t.getTitle().equals("x"));
        verify(mockService).list(eq(kw), eq(done), any(Pageable.class));
    }
}
