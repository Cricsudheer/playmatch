package com.example.playmatch.api.controller;

import com.example.playmatch.api.model.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
public class HealthController implements HealthApi {

    @Override
    public ResponseEntity<HealthResponse> _checkLongPollingHealth() {
        HealthResponse response = new HealthResponse();
        response.setStatus(HealthResponse.StatusEnum.UP);
        response.setTimestamp(OffsetDateTime.now());
        response.setMessage("Service is ready for long polling");
        return ResponseEntity.ok(response);
    }
}
