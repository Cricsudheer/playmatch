package com.example.playmatch.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> healthCheck() {
        log.info("Health check request received");
        Map<String, String> response = new HashMap<>();
        response.put("message", "hello am up");
        response.put("status", "ok");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("Ping request received");
        return ResponseEntity.ok("hello am up");
    }
}

