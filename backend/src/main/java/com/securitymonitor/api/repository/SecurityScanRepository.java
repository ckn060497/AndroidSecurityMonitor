package com.securitymonitor.api.repository;

import com.securitymonitor.api.model.SecurityScan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SecurityScanRepository extends JpaRepository<SecurityScan, Long> {
    List<SecurityScan> findTop20ByDeviceIdOrderByScannedAtDesc(Long deviceId);
}
