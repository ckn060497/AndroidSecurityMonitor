
package com.securitymonitor.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "security_scans")
public class SecurityScan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id")
    private Device device;

    private Instant scannedAt;
    private int riskScore;
    private String riskLevel;

    @Column(columnDefinition = "TEXT")
    private String reportJson;

    public SecurityScan() {}

    public Long getId() { return id; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
    public Instant getScannedAt() { return scannedAt; }
    public void setScannedAt(Instant scannedAt) { this.scannedAt = scannedAt; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getReportJson() { return reportJson; }
    public void setReportJson(String reportJson) { this.reportJson = reportJson; }
}
