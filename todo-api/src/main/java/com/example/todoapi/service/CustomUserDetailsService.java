package com.example.todoapi.service;

import com.example.todoapi.entity.User;
import com.example.todoapi.repository.UserRepository;

import lombok.AllArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collection;

/** DB からユーザーを読み込んで UserDetails をJwtTokenProviderに返す */
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));
        // ユーザーに紐づく権限情報変換
        var authorities = user.getRoles().stream()
                .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.name()))
                .toList();
        // UserDetailsをextendsしたLoginUser（id付きUserDetails）を返却
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(), authorities);
    }

    /** 同ファイル内の“id付きUserDetails”実装 */
    public static class LoginUser extends org.springframework.security.core.userdetails.User {
        private final Long id;

        public LoginUser(Long id, String username, String password,
                Collection<? extends GrantedAuthority> authorities) {
            super(username, password, authorities);
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}