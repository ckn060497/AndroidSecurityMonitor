package com.securitymonitor.api.service;

import com.securitymonitor.api.dto.ScanRequest;
import com.securitymonitor.api.model.Device;
import com.securitymonitor.api.model.SecurityScan;
import com.securitymonitor.api.repository.SecurityScanRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ScanService {
    private final SecurityScanRepository repository;
    private final DeviceService deviceService;

    public ScanService(SecurityScanRepository repository, DeviceService deviceService) {
        this.repository = repository;
        this.deviceService = deviceService;
    }

    public SecurityScan save(Long deviceId, ScanRequest request) {
        Device device = deviceService.get(deviceId);
        SecurityScan scan = new SecurityScan();
        scan.setDevice(device);
        scan.setScannedAt(Instant.now());
        scan.setRiskScore(request.riskScore());
        scan.setRiskLevel(request.riskLevel());
        scan.setReportJson(request.reportJson());
        deviceService.touch(device);
        return repository.save(scan);
    }

    public List<SecurityScan> history(Long deviceId) {
        deviceService.get(deviceId);
        return repository.findTop20ByDeviceIdOrderByScannedAtDesc(deviceId);
    }
}
