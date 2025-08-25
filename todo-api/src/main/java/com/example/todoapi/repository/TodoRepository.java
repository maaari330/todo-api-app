package com.example.todoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.todoapi.entity.Todo;
import java.util.List;

/**
 * Todo エンティティの DB 操作を担うリポジトリ
 * JpaRepository<Todo, Long> を継承することで、
 * - save() : 登録・更新
 * - findAll() : 全件取得
 * - findById() : ID 検索
 * - deleteById(): 削除
 * などの基本メソッドが自動で使える。
 * 
 * ワードルール例
 * キーワード 意味 例
 * findBy 検索メソッドの先頭 findByTitle(...)
 * Containing SQL の LIKE %…% 相当 findByTitleContaining(keyword)
 * And, Or 複数条件の結合 findByTitleContainingAndDone(...)
 * LessThan, GreaterThan < / > 条件 findByAgeLessThan(30)
 * OrderByXXXDesc/Asc ソート findByDoneOrderByCreatedAtDesc()
 */

// JpaRepository での基本メソッドに加え、JpaSpecificationExecutor による動的検索メソッドが利用可能

public interface TodoRepository extends JpaRepository<Todo, Long>, JpaSpecificationExecutor<Todo> {
        // リマインド設定があるがまだ未通知のタスクで、（締切 - 現在）>= リマインド設定時間 のtodo.idを取得
        // NotificationServiceで使用
        @Query(value = "SELECT * FROM todos t " +
                        "WHERE t.due_date IS NOT NULL " +
                        "  AND t.remind_offset_minutes IS NOT NULL " +
                        "  AND t.notified_at IS NULL " +
                        "  AND TIMESTAMPDIFF(MINUTE, NOW(), t.due_date) BETWEEN 0 AND t.remind_offset_minutes", nativeQuery = true) // 今（NOW()）から締切（t.due_date）までの差分を“分”で返す
        List<Long> findIdsDueForNotification();

        // 対象IDを一括で notified_at 埋める（競合時にも安全）
        // NotificationServiceで使用
        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query(value = "UPDATE todos SET notified_at = :now " +
                        "WHERE id IN (:ids) AND notified_at IS NULL", nativeQuery = true)
        int markNotifiedByIds(@Param("ids") List<Long> ids, @Param("now") java.time.LocalDateTime now); // 更新された行数が戻り値
}
