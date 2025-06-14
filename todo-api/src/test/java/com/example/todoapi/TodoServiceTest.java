package com.example.todoapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    TodoRepository repo;

    @InjectMocks
    TodoService service;

    @Test
    void update_existingId_title() {
        // 準備
        Long id = 1L;
        Todo original = new Todo(id, "before", false);
        UpdateTodoRequest req = new UpdateTodoRequest();
        req.setTitle("after");

        when(repo.findById(id)).thenReturn(Optional.of(original));
        when(repo.save(any(Todo.class))).thenAnswer(inv -> inv.getArgument(0));

        // 実行
        Todo updated = service.update(id, req);

        // 検証
        assertEquals("after", updated.getTitle());
        verify(repo).save(original);
    }

    @Test
    void update_notFoundId_throws404() {
        Long id = 999L;
        UpdateTodoRequest req = new UpdateTodoRequest();
        req.setTitle("x");

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
        Todo toCreate = new Todo(null, "task", false);
        Todo saved = new Todo(1L, "task", false);
        when(repo.save(toCreate)).thenReturn(saved);

        Todo result = service.create(toCreate);  // もし引数が title だけなら
        assertThat(result.getId()).isEqualTo(1L);
        verify(repo).save(toCreate);
    }

    @Test
    void list_withAndWithoutKeyword() {
        // without keyword
        var all = List.of(new Todo(1L,"A",false),
                          new Todo(2L,"B",false));
        when(repo.findAll()).thenReturn(all);
        assertThat(service.list(null)).isEqualTo(all);
        verify(repo).findAll();

        // with keyword
        var filtered = List.of(new Todo(3L,"foo",false));
        when(repo.findByTitleContaining("foo")).thenReturn(filtered);
        assertThat(service.list("foo")).isEqualTo(filtered);
        verify(repo).findByTitleContaining("foo");
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
        Todo before = new Todo(1L,"t",false);
        when(repo.findById(1L)).thenReturn(Optional.of(before));
        when(repo.save(before)).thenReturn(before);

        Todo result = service.toggleDone(1L);
        assertThat(result.isDone()).isTrue();

        verify(repo).save(before);
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