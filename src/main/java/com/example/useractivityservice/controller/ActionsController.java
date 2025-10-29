package com.example.useractivityservice.controller;


import com.example.useractivityservice.dto.ActionsDto;
import com.example.useractivityservice.dto.NotificationsDto;
import com.example.useractivityservice.service.ActionsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ActionsController {

    @Autowired
    private ActionsService actionService;

    @PostMapping("/actions")
    public ResponseEntity<Void> addAction(@Valid @RequestBody ActionsDto dto) {
        actionService.addAction(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationsDto>> getNotifications() {
        return ResponseEntity.ok(actionService.getAllNotifications());
    }
}
