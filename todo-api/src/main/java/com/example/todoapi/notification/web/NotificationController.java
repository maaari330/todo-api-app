package com.example.todoapi.notification.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.todoapi.notification.service.NotificationService;
import com.example.todoapi.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    /** 直近10分での通知済みの通知を最大5件（ユーザー別／DB参照） */
    @GetMapping("/in-app/recent")
    public List<NotificationService.InAppMessage> recent(
            @AuthenticationPrincipal CustomUserDetailsService.LoginUser user) {
        return notificationService.recentByUser(user.getId(), 10, 5);
    }
}
