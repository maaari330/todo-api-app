package com.example.todoapi.push.dto;

public record SubscribeRequest(
        String endpoint,
        String p256dh,
        String auth,
        String userAgent) {
}