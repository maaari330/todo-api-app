package com.example.todoapi.service;

import com.example.todoapi.dto.auth.LoginRequest;
import com.example.todoapi.dto.auth.SignupRequest;
import com.example.todoapi.dto.auth.UserDto;
import com.example.todoapi.entity.User;
import com.example.todoapi.entity.Role;
import com.example.todoapi.repository.UserRepository;
import com.example.todoapi.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 安全にハッシュ化（＝エンコード）し、後で照合できるようにする
    private final JwtTokenProvider jwtTokenProvider;

    // 新規ユーザー登録
    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("ユーザー名が既に使用されています");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(new HashSet<>(List.of(Role.ROLE_USER))); // 固定ではADMIN権限ではなく一般ユーザー権限
        userRepository.save(user);
    }

    // ログイン機能
    @Transactional
    public String login(LoginRequest request) {
        // ユーザー存在確認
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new IllegalArgumentException("ユーザー名またはパスワードが間違っています"));
        // パスワード一致確認
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("ユーザー名またはパスワードが間違っています");
        }
        // JWTトークン生成（JSON Web Token）：{Header}.{Payload}.{Signature}
        // ユーザー名と権限セットをトークンの中身（ペイロード）として埋め込む
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
    }

    // 現在認証済みユーザー情報を返す
    public UserDto me(UserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "認証情報がありません");
        }
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getRoles().stream().map(Role::name).toList()
        );
    }
}
