package com.example.todoapi.notification.service;

import org.springframework.data.domain.PageRequest;
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

    /** 1)通知対象を集める（ID→エンティティ） */
    @Transactional(readOnly = true)
    public List<Todo> collectDueTodos() {
        var ids = todoRepository.findIdsDueForNotification();
        return ids.isEmpty() ? List.of() : todoRepository.findAllById(ids); // idに紐づくTodoエンティティを返す
    }

    /** 2)送れたIDだけバックエンド内で既読化（at-least-once） */ // → 1,2)はNotificationJobでバックエンド内で実行
    @Transactional
    public void markNotifiedByIds(List<Long> ids) {
        if (!ids.isEmpty()) {
            todoRepository.markNotifiedByIds(ids, LocalDateTime.now());
        }
    }

    /** 3) In-app用：直近N分の通知をユーザー別にフロントへ返す */ // 3)はNotificationControllerで実行
    @Transactional(readOnly = true)
    public List<InAppMessage> recentByUser(Long userId, int minutes, int limit) {
        var since = LocalDateTime.now().minusMinutes(minutes); // now - minutes = since(直近N分前)
        var slice = todoRepository.findRecentNotifiedByUser(userId, since, PageRequest.of(0, limit));
        return slice.getContent().stream() // getContent() → List<Todo> → map内でList<Todo>からList<InAppMessage>に変更
                .map(t -> new InAppMessage(t.getId(),
                        "まもなく期限：「" + t.getTitle() + "」",
                        t.getDueDate(),
                        t.getNotifiedAt()))
                .toList();
    }

    /** アプリ内通知表示用の不変 DTO */
    public record InAppMessage(Long todoId, String text, LocalDateTime dueDate, LocalDateTime notifiedAt) {
        /**
         * DBから取った Todo を Service で整形した DTO
         * 
         * 【record により自動生成されるもの】
         * - private final フィールド：todoId, text, dueDate, notifiedAt
         * - コンストラクタ： public InAppMessage(Long todoId, String text, LocalDateTime
         * dueDate,LocalDateTime notifiedAt)
         * - アクセサ（ゲッター相当・名前はフィールド名そのまま）：todoId(), text(), dueDate(), notifiedAt()
         * - equals()/hashCode()：全コンポーネントを基に生成
         * - toString()
         */
    }
}