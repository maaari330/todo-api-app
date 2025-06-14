package com.example.todoapi;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TodoService {
    private final TodoRepository repo;

    public TodoService(TodoRepository repo) {
        this.repo = repo;
    }

    /** 新規作成 */
    @Transactional
    public Todo create(Todo t) {
        return repo.save(t);
    }

    /** 一覧取得 */
    @Transactional(readOnly = true)
    public List<Todo> list(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return repo.findAll();
        }
        return repo.findByTitleContaining(keyword);
    }

    /** 更新 */
    @Transactional
    public Todo update(Long id, UpdateTodoRequest req) {
        Todo t = repo.findById(id)
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));
        t.setTitle(req.getTitle());
        if (req.getDone() != null) {
            t.setDone(req.getDone());
        }
        return repo.save(t);
    }

    /** 削除 */
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    /** 完了フラグの切り替え */
    @Transactional
    public Todo toggleDone(Long id) {
        Todo t = repo.findById(id)
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));
        t.setDone(!t.isDone());
        return repo.save(t);
    }
}
