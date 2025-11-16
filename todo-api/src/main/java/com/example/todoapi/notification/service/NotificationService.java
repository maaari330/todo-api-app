package com.example.todoapi.notification.service;

import com.example.todoapi.repository.TodoRepository;
import com.example.todoapi.entity.Todo;
import com.example.todoapi.util.TimeZoneConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
            todoRepository.markNotifiedByIds(ids, LocalDateTime.now(ZoneOffset.UTC));
        }
    }

    /* 3) In-app用：直近N分の通知をユーザー別にページングで取り出して、DTOに整形してフロントへ返す */ // 3)はNotificationControllerで実行
    @Transactional(readOnly = true)
    public Paged<InAppMessage> findRecentByUserPaged(Long userId, LocalDateTime since, int page, int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var slice = todoRepository.findRecentNotifiedByUser(userId, since, pageable);
        var content = slice.getContent().stream().map(NotificationService::toMessage).toList();
        // Todo を画面用DTOに toMessageを使って変換
        return new Paged<>(content, slice.hasNext()); // アイテム本体(InAppMessage) + まだ次があるかの2点だけを返す
    }

    /* 3)で使用：表示／API返却用の軽量コンテナ */
    public record Paged<T>(List<T> content, boolean hasNext) {
        // Springの型（Slice や Page）を外部に漏らさず、必要最小限の情報だけを返す。
    }

    /* 3)で使用：アプリ内通知表示用の不変 DTO */
    public record InAppMessage(Long todoId, String title, String body, String url, Instant createdAt) {
        /**
         * DBから取った Todo を Service で整形した DTO
         * - エンティティ（Todo）を直接返さず、必要な項目に限定して漏洩を防ぐ
         * 
         * 【record により自動生成されるもの】
         * - private final フィールド：各引数ごと
         * - コンストラクタ： public InAppMessage(Long todoId, String title, String body, String
         * url, Instant createdAt)
         * - アクセサ（ゲッター相当・名前はフィールド名そのまま）：todoId(), title(), body(), url(), createdAt()
         * - equals()/hashCode()：全コンポーネントを基に生成
         * - toString()
         */
    }

    /* 3)で使用：Todo → InAppMessage 変換 */
    private static InAppMessage toMessage(Todo t) {
        Long id = t.getId();
        String title = "My TODO Task Manager まもなく期限：「" + nullSafe(t.getTitle()) + "」";
        String body = (t.getDueDate() != null) // 期限
                ? "期限: " + TimeZoneConverter.toJtc(t.getDueDate()).toString().replace('T', ' ')
                : "期限未設定";
        String url = "/todos?open=" + id; // /todos一覧内のタスクを開くルーティング
        Instant createdAt = (t.getNotifiedAt() != null) // 通知打刻
                ? t.getNotifiedAt().atZone(ZoneOffset.UTC).toInstant() // ローカル時刻→（地域適用）→UTC(Instant) の瞬間へ
                : Instant.now();
        return new InAppMessage(id, title, body, url, createdAt);
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }
}