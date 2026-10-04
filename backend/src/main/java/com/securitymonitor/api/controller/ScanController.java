package com.securitymonitor.api.controller;

import com.securitymonitor.api.dto.ScanRequest;
import com.securitymonitor.api.model.SecurityScan;
import com.securitymonitor.api.service.ScanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices/{deviceId}/scan")
public class ScanController {
    private final ScanService service;

    public ScanController(ScanService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SecurityScan> submit(
            @PathVariable Long deviceId,
            @Valid @RequestBody ScanRequest request) {
        return ResponseEntity.ok(service.save(deviceId, request));
    }

    @GetMapping
    public List<SecurityScan> history(@PathVariable Long deviceId) {
        return service.history(deviceId);
    }
}
