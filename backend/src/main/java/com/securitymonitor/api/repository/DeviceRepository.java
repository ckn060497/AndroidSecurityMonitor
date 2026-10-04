package com.securitymonitor.api.repository;

import com.securitymonitor.api.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceKey(String deviceKey);
}
