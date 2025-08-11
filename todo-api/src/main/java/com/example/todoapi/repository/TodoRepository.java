package com.example.todoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.example.todoapi.entity.Todo;
/**
 * Todo エンティティの DB 操作を担うリポジトリ
 * JpaRepository<Todo, Long> を継承することで、
 *   - save()      : 登録・更新
 *   - findAll()   : 全件取得
 *   - findById()  : ID 検索
 *   - deleteById(): 削除
 * などの基本メソッドが自動で使える。
 * 
 * ワードルール例
 * キーワード	意味	例
 * findBy	検索メソッドの先頭	findByTitle(...)
 * Containing	SQL の LIKE %…% 相当	findByTitleContaining(keyword)
 * And, Or	複数条件の結合	findByTitleContainingAndDone(...)
 * LessThan, GreaterThan	< / > 条件	findByAgeLessThan(30)
 * OrderByXXXDesc/Asc	ソート	findByDoneOrderByCreatedAtDesc()
 */

// JpaRepository での基本メソッドに加え、JpaSpecificationExecutor による動的検索メソッドが利用可能

public interface TodoRepository extends JpaRepository<Todo, Long> ,JpaSpecificationExecutor<Todo> { }
