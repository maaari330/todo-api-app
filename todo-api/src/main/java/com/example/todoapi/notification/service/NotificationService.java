package com.example.todoapi.notification.service;

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

    @Transactional(readOnly = true)
    public List<Todo> collectDueTodos() {
        var ids = todoRepository.findIdsDueForNotification();
        if (ids.isEmpty())
            return List.of();
        return todoRepository.findAllById(ids); // idに紐づくTodoエンティティを返す
    }

    @Transactional
    public void markNotifiedByIds(List<Long> ids) {
        if (ids.isEmpty())
            return;
        todoRepository.markNotifiedByIds(ids, LocalDateTime.now());
    }
}