
package com.example.todoapi.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Data
@Table(name = "todos")
@NoArgsConstructor // 引数なしコンストラクタを生成
@AllArgsConstructor // 全フィールドを引数に取るコンストラクタを生成
public class Todo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  private boolean done;

  @Column(name = "due_date")
  private LocalDateTime dueDate;

  @Enumerated(EnumType.STRING) // enum の名前（文字列）をDBに保存
  @Column(name = "repeat_type", nullable = false) // DBにrepeat_type という 非NULLの文字列カラムが作成される
  private RepeatType repeatType;

  @ManyToOne(fetch = FetchType.LAZY) // Todo（多）対 User owner（１）
  @JoinColumn(name = "user_id", nullable = false) // 外部キー：todoエンティティ.getOwner().getId()== Userエンティティ.getId()
  private User owner;

  @ManyToOne(fetch = FetchType.LAZY) // Todo（多）対 Category category（１）
  @JoinColumn(name = "category_id") // 外部キー：所有todoエンティティ.getCategory().getId()== Categoryエンティティ.getId()
  private Category category;

  @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable( // 多対多のため中間テーブルを用意
      name = "todo_tag", // 中間テーブル
      joinColumns = @JoinColumn(name = "todo_id"), // 自エンティティ側の外部キー列指定
      inverseJoinColumns = @JoinColumn(name = "tag_id") // 相手エンティティ側の外部キー列指定
  )
  private Set<Tag> tags = new HashSet<>();

  @Column(name = "remind_offset_minutes")
  private Integer remindOffsetMinutes; // リマインド設定（何分以内）

  @Column(name = "notified_at")
  private LocalDateTime notifiedAt; // リマインド送信済みフラグ
}
