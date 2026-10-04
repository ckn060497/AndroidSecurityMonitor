package com.securitymonitor.api.controller;

import com.securitymonitor.api.dto.RegisterDeviceRequest;
import com.securitymonitor.api.model.Device;
import com.securitymonitor.api.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<Device> register(@Valid @RequestBody RegisterDeviceRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @GetMapping
    public List<Device> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Device get(@PathVariable Long id) {
        return service.get(id);
    }
}
