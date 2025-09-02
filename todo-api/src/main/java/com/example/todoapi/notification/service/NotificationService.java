package com.example.todoapi.notification.service;

import com.example.todoapi.repository.TodoRepository;
import com.example.todoapi.notification.service.NotificationService;
import com.example.todoapi.entity.Todo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.time.LocalDateTime;

/** 通知の業務ロジック集約（通知対象取得・送信後の打刻・履歴取得など） */
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final TodoRepository todoRepository;

    /* 1)通知対象を集める（ID→エンティティ） */
    @Transactional(readOnly = true)
    public List<Todo> collectDueTodos() {
        var ids = todoRepository.findIdsDueForNotification();
        return ids.isEmpty() ? List.of() : todoRepository.findAllById(ids); // idに紐づくTodoエンティティを返す
    }

    /* 2)送れたIDだけバックエンド内で既読化（at-least-once） */ // → 1,2)はNotificationJobでバックエンド内で実行
    @Transactional
    public void markNotifiedByIds(List<Long> ids) {
        if (!ids.isEmpty()) {
            todoRepository.markNotifiedByIds(ids, LocalDateTime.now());
        }
    }

    /* 3) In-app用：直近N分の通知をユーザー別にページングで取り出して、DTOに整形してフロントへ返す */ // 3)はNotificationControllerで実行
    @Transactional(readOnly = true)
    public Paged<InAppMessage> findRecentByUserPaged(Long userId, LocalDateTime since, int page, int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var slice = todoRepository.findRecentNotifiedByUser(userId, since, pageable);
        var content = slice.getContent().stream().map(NotificationService::toMessage).toList(); // Todo を画面用DTOに変換
        return new Paged<>(content, slice.hasNext()); // アイテム本体(InAppMessage) + まだ次があるかの2点だけを返す
    }

    /* 3)で使用：表示／API返却用の軽量コンテナ */
    public record Paged<T>(List<T> content, boolean hasNext) {
        // Springの型（Slice や Page）を外部に漏らさず、必要最小限の情報だけを返す。
    }

    /* 3)で使用：アプリ内通知表示用の不変 DTO */
    public record InAppMessage(Long todoId, String text, LocalDateTime dueDate, LocalDateTime notifiedAt) {
        /**
         * DBから取った Todo を Service で整形した DTO
         * - エンティティ（Todo）を直接返さず、必要な項目に限定して漏洩を防ぐ
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

    /* 3)で使用：Todo → InAppMessage 変換 */
    private static InAppMessage toMessage(Todo t) {
        return new InAppMessage(
                t.getId(),
                "まもなく期限：「" + t.getTitle() + "」",
                t.getDueDate(),
                t.getNotifiedAt());
    }
}