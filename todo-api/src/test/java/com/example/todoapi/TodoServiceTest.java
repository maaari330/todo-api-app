package com.example.todoapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.todoapi.dto.todo.CreateTodoRequest;
import com.example.todoapi.dto.todo.UpdateTodoRequest;
import com.example.todoapi.repository.TodoRepository;
import com.example.todoapi.service.TodoService;
import com.example.todoapi.entity.Todo; 
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    TodoRepository repo;

    @InjectMocks
    TodoService service;

    @Test
    void update_existingId_titleAndDate() {
        // 準備
        Long id = 1L;
        LocalDateTime originalDue = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime newDue = LocalDateTime.of(2025, 12, 31, 18, 10);
        Todo original = new Todo(id, "before", false, originalDue);
        UpdateTodoRequest req = new UpdateTodoRequest();
        req.setTitle("after");
        req.setDueDate(newDue);
        when(repo.findById(id)).thenReturn(Optional.of(original));
        when(repo.save(any(Todo.class))).thenAnswer(inv -> inv.getArgument(0));
        // 実行
        Todo updated = service.update(id, req);
        // 返り値の検証
        assertThat(updated.getTitle()).isEqualTo("after");
        assertThat(updated.getDueDate()).isEqualTo(newDue);
        // repo.saveに渡されたTodoインスタンスの中身を検証
        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        verify(repo).save(captor.capture());
        Todo passed = captor.getValue();
        assertThat(passed)
            .usingRecursiveComparison()
            .isEqualTo(new Todo(1L, "after", false, newDue)); 
    } 

    @Test
    void update_onlyDueDateField_updatesDueDateOnly() {
        Long id = 2L;
        LocalDateTime originalDue = LocalDateTime.of(2025, 1, 1, 0, 0);
        Todo original = new Todo(id, "task", false, originalDue);

        UpdateTodoRequest req = new UpdateTodoRequest();
        LocalDateTime newDue = LocalDateTime.of(2026, 6, 30, 12, 0);
        req.setDueDate(newDue);  // dueDate のみ変更

        when(repo.findById(id)).thenReturn(Optional.of(original));
        when(repo.save(any(Todo.class))).thenAnswer(inv -> inv.getArgument(0));

        Todo updated = service.update(id, req);
        // 返り値の検証
        assertThat(updated.getDueDate()).isEqualTo(newDue);
        assertThat(updated.getTitle()).isEqualTo("task");
        assertThat(updated.isDone()).isFalse();
        // repo.saveに渡されたTodoインスタンスの中身を検証
        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        verify(repo).save(captor.capture());
        Todo passed = captor.getValue();
        assertThat(passed)
            .usingRecursiveComparison()
            .isEqualTo(new Todo(2L, "task", false, newDue)); 
    }

    @Test
    void update_notFoundId_throws404() {
        Long id = 999L;
        UpdateTodoRequest req = new UpdateTodoRequest();
        req.setTitle("x");
        // 実行
        when(repo.findById(id)).thenReturn(Optional.empty());
        // 例外が投げられることを検証
        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> service.update(id, req)
        );
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void create_savesAndReturnsTodo() {
        CreateTodoRequest req = new CreateTodoRequest();
        LocalDateTime date=LocalDateTime.of(2025, 6, 25, 18, 10);
        req.setTitle("task");
        req.setDone(false);
        req.setDueDate(date);
        Todo saved = new Todo(1L, "task", false, date);
        when(repo.save(any(Todo.class))).thenReturn(saved);
        Todo result = service.create(req);  
        // 返り値の検証
        assertThat(result.getId()).isEqualTo(1L);
        // repo.saveに渡されたTodoインスタンスの中身を検証
        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        verify(repo).save(captor.capture());         
        Todo passed = captor.getValue();
        assertThat(passed)
            .usingRecursiveComparison()
            .isEqualTo(new Todo(null, "task", false, date)); // 永続化前は null
    }

    @Test
    void creates_savesWithoutDueDate() {
        CreateTodoRequest req = new CreateTodoRequest();
        req.setTitle("task");
        req.setDone(false);
        req.setDueDate(null);
        // 実行
        when(repo.save(any(Todo.class))).thenAnswer(inv -> {
            Todo orig = inv.getArgument(0);
            // 直接reqインスタンスのidを書き換えるとArgumentCaptorでidが書き換わる前の純粋な引数を検証できないのでコピーしてnew Todoを作成
            return new Todo(5L, 
                    orig.getTitle(),
                    orig.isDone(),
                    orig.getDueDate());
        });
        // 検証
        Todo result = service.create(req);
        // 返り値の検証
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getTitle()).isEqualTo("task");
        assertThat(result.isDone()).isFalse();
        assertThat(result.getDueDate()).isNull();
        // repo.saveに渡されたTodoインスタンスの中身を検証
        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        verify(repo).save(captor.capture());         
        Todo passed = captor.getValue();
        assertThat(passed)
            .usingRecursiveComparison()
            .isEqualTo(new Todo(null, "task", false, null)); // 永続化前は null
    }

    @ParameterizedTest(name = "Scenario {index}: keyword={0}, done={1}, method={2}")
    @MethodSource("provideListArguments")
    void list_parameterized(String keyword, Boolean done, String repoMethod, Pageable pageable) {
        // Arrange: stub appropriate repository method
        Page<Todo> page = new PageImpl<>(List.of(new Todo(100L, "X", false, null)), pageable, 1);
        switch (repoMethod) {
            case "findAll":
                when(repo.findAll(pageable)).thenReturn(page);
                break;
            case "findByTitleContaining":
                when(repo.findByTitleContaining(keyword, pageable)).thenReturn(page);
                break;
            case "findByDone":
                when(repo.findByDone(done, pageable)).thenReturn(page);
                break;
            case "findByTitleContainingAndDone":
                when(repo.findByTitleContainingAndDone(keyword, done, pageable)).thenReturn(page);
                break;
            default:
                throw new IllegalArgumentException(repoMethod);
        }
        // Act
        Page<Todo> result = service.list(keyword, done, pageable);
        assertThat(result).isSameAs(page);
        assertThat(result.getContent())
            .extracting(Todo::getId, Todo::getTitle)
            .containsExactly(tuple(100L, "X"));
        // Assert: content and delegation
        assertThat(result.getContent()).hasSize(1);
        switch (repoMethod) {
            case "findAll":
                verify(repo).findAll(pageable);
                break;
            case "findByTitleContaining":
                verify(repo).findByTitleContaining(keyword, pageable);
                break;
            case "findByDone":
                verify(repo).findByDone(done, pageable);
                break;
            case "findByTitleContainingAndDone":
                verify(repo).findByTitleContainingAndDone(keyword, done, pageable);
                break;
        }
    }

    static Stream<Arguments> provideListArguments() {
        return Stream.of(
            Arguments.of(null, null, "findAll", Pageable.unpaged()),
            Arguments.of("foo", null, "findByTitleContaining", PageRequest.of(0, 5)),
            Arguments.of(null, true, "findByDone", PageRequest.of(1, 3)),
            Arguments.of("bar", false, "findByTitleContainingAndDone", PageRequest.of(2, 2))
        );
    }


    @Test
    void delete_existingId_deletesWithoutException() {
        // service.delete は void。特に戻り値はないので例外が出ないことだけを検証
        doNothing().when(repo).deleteById(1L);
        service.delete(1L);
        verify(repo).deleteById(1L);
    }

    @Test
    void delete_nonexistentId_throws() {
        doThrow(new EntityNotFoundException()).when(repo).deleteById(99L);
        assertThatThrownBy(() -> service.delete(99L))
        .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void toggleDone_flipsAndSaves() {
        LocalDateTime date=LocalDateTime.of(2025, 6, 25, 18, 10);
        Todo before = new Todo(1L,"t",false, date);
        when(repo.findById(1L)).thenReturn(Optional.of(before));
        when(repo.save(before)).thenReturn(before);
        Todo result = service.toggleDone(1L);
        // 返り値の検証
        assertThat(result.isDone()).isTrue();
        // サービス層がリポジトリに渡した引数（save(...) の直前の Todo）に done==true がセットされているか
        verify(repo).save(argThat(t -> t.isDone()));
    }

    @Test
    void toggleDone_notFound_throws404() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.toggleDone(99L))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException)ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
}