package com.example.todoapi.Specification;

import com.example.todoapi.entity.Todo;
import com.example.todoapi.entity.Tag;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

/** Specification<T> を使って、動的にクエリ条件式を組み立てるユーティリティ　*/
public class TodoSpecifications {
    // 0）オーナー ID が一致するものだけ
    public static Specification<Todo> ownerIs(Long ownerId) {
        return (root, query, cb) ->
            ownerId == null
                ? null
                : cb.equal(root.get("owner").get("id"), ownerId); // SQL におけるWHERE todo.user_id = :ownerIdという条件式 （Userエンティティにおけるid）
    }
    // 1) タイトルフィルタ
    public static Specification<Todo> titleContains(String keyword) { 
        return (root, query, cb) -> 
            keyword == null
              ? null
              : cb.like(root.get("title"), "%" + keyword + "%"); // SQL におけるWHERE title LIKE '%foo%'という条件式
    }
    // 2) 完了ステータスフィルタ
    public static Specification<Todo> doneIs(Boolean done) {
        return (root, query, cb) ->
            done == null
              ? null
              : cb.equal(root.get("done"), done); // SQL におけるWHERE done = :doneという条件式
    }
    // 3) カテゴリ絞り込み
    public static Specification<Todo> categoryIs(Long categoryId) {
        return (root, query, cb) ->
            categoryId == null
              ? null
              : cb.equal(root.get("category").get("id"), categoryId); // SQL におけるWHERE category_id = :categoryIdという条件式
    }
    // 4) タグ絞り込み
    public static Specification<Todo> hasTags(Set<Long> tagIds) {
        return (root, query, cb) -> {
            if (tagIds == null || tagIds.isEmpty()) return null;
            Join<Todo,Tag> tags = root.joinSet("tags"); // Todo と Tag の多対多関係を SQL の JOIN にマッピング
            if (query != null) {
                query.distinct(true); // 重複した行を排除して、一意（ユニーク）な結果セットだけを取得
            }
            return tags.get("id").in(tagIds);
        };
    }
}