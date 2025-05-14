package com.example.todoapi;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * Todo エンティティの DB 操作を担うリポジトリです。
 * JpaRepository<Todo, Long> を継承することで、
 *   - save()      : 登録・更新
 *   - findAll()   : 全件取得
 *   - findById()  : ID 検索
 *   - deleteById(): 削除
 * などの基本メソッドが自動で使えます。
 */

public interface TodoRepository extends JpaRepository<Todo, Long> {
     /**
     * title カラムに keyword を含むレコードを返す。
     * SQL では WHERE title LIKE %keyword% に相当します。
     */
    List<Todo> findByTitleContaining(String keyword);
}
