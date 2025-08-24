package com.example.todoapi.service;

import org.springframework.stereotype.Service;

import com.example.todoapi.repository.TodoRepository;
import org.springframework.transaction.annotation.Transactional;
import com.example.todoapi.entity.Todo;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final TodoRepository todoRepository;

    @Transactional
    public List<Todo> collectDueTodosAndMarkNotified() {
        var ids = todoRepository.findIdsDueForNotification();
        if (ids.isEmpty())
            return List.of();
        var now = LocalDateTime.now();
        // 競合があっても notified_at IS NULL 条件で二重更新を防げる
        todoRepository.markNotifiedByIds(ids, now);
        // 実際に通知に使うエンティティ（タイトル等が必要）
        return todoRepository.findAllById(ids);
    }
}