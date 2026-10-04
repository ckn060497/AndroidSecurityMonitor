package com.securitymonitor.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ScanRequest(
        @Min(0) @Max(100) int riskScore,
        @NotBlank String riskLevel,
        @NotBlank String reportJson
) {}
