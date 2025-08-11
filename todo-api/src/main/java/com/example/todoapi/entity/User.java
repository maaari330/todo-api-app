package com.example.todoapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL の AUTO_INCREMENT機能を使って、一意の連番を割り振る 
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;  // ハッシュ化したパスワードを保存

    @ElementCollection(fetch = FetchType.EAGER) //別テーブルに値を保持する仕組み、Set<Role>（複数ロール）を許容する設計（多くの現実アプリがこの設計）
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id")) // 中間テーブルを作成。user_idでuser_rolesとusersテーブルを紐づける。user\roles.user_idが外部キー
    @Column(name = "roles") // user_rolesでSet<Role> rolesをrolesというカラム名で管理
    private Set<Role> roles; // 権限情報（ROLE_USER, ROLE_ADMIN等）
}
