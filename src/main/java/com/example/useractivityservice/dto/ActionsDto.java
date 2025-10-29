package com.example.useractivityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ActionsDto {

    @NotBlank
    private Long userId;
    @NotBlank
    private String actionType;
    private LocalDateTime timestamp;
}
