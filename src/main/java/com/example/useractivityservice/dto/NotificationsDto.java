package com.example.useractivityservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationsDto {
    private Long id;
    private Long userId;
    private String message;
    private LocalDateTime createdAt;
}
