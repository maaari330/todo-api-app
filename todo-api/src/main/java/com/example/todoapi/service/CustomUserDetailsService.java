package com.example.todoapi.service;

import com.example.todoapi.entity.User;
import com.example.todoapi.repository.UserRepository;

import lombok.AllArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** DB からユーザーを読み込んで UserDetails をJwtTokenProviderに返す */
@Service 
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() ->
                new UsernameNotFoundException("ユーザーが見つかりません: " + username)
            );
        // User エンティティを Spring Security の UserDetails に変換するユーティリティメソッド
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())        // パスワードは既にハッシュ化されている前提
            .authorities(
                user.getRoles().stream()
                    .map(Enum::name)
                    .toArray(String[]::new)
            )
            .build();
    }
}