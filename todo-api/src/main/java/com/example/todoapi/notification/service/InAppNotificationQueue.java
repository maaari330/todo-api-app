package com.example.todoapi.service;

import org.springframework.stereotype.Component;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;
import java.time.LocalDateTime;

//  直近200件のアプリ内通知メッセージを保持するキュー
@Component
public class InAppNotificationQueue {
    private static final int LIMIT = 200;
    private final Deque<InAppMessage> deque = new ArrayDeque<>();

    // 先頭から新しい→古い の順でDTOを保持するためのキュー操作メソッド
    public synchronized void push(InAppMessage msg) {
        deque.addFirst(msg);
        while (deque.size() > LIMIT)
            deque.removeLast();
    }

    // InAppMessageキューから通知を取り出すメソッド
    public synchronized List<InAppMessage> recent() {
        return List.copyOf(deque);
    }

    public record InAppMessage(Long todoId, String text, LocalDateTime dueDate, Long userId) {
        /**
         * todoId: どのToDoに関する通知か
         * text: 通知本文（表示用テキスト）
         * dueDate: そのToDoの締切
         * userId: 対象ユーザー
         */
    }
}