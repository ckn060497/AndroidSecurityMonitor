package com.securitymonitor.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterDeviceRequest(
        @NotBlank String deviceKey,
        String deviceName,
        String androidVersion,
        String manufacturer,
        String model,
        String appVersion
) {}
