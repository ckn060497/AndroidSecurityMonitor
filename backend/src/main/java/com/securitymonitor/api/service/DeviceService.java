package com.securitymonitor.api.service;

import com.securitymonitor.api.dto.RegisterDeviceRequest;
import com.securitymonitor.api.model.Device;
import com.securitymonitor.api.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DeviceService {
    private final DeviceRepository repository;

    public DeviceService(DeviceRepository repository) {
        this.repository = repository;
    }

    public Device register(RegisterDeviceRequest request) {
        Device device = repository.findByDeviceKey(request.deviceKey()).orElseGet(Device::new);
        device.setDeviceKey(request.deviceKey());
        device.setDeviceName(request.deviceName());
        device.setAndroidVersion(request.androidVersion());
        device.setManufacturer(request.manufacturer());
        device.setModel(request.model());
        device.setAppVersion(request.appVersion());
        if (device.getRegisteredAt() == null) device.setRegisteredAt(Instant.now());
        device.setLastSeenAt(Instant.now());
        return repository.save(device);
    }

    public Device get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
    }

    public List<Device> all() {
        return repository.findAll();
    }

    public Device touch(Device device) {
        device.setLastSeenAt(Instant.now());
        return repository.save(device);
    }
}
