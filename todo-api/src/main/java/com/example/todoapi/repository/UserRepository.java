package com.example.todoapi.repository;

import com.example.todoapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // ユーザー検索
    Optional<User> findByUsername(String username);
}