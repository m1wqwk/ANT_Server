package com.antivirus.server.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class LicenseActivateRequest {
    @NotBlank(message = "Activation key is required")
    private String activationKey;

    @NotBlank(message = "Device MAC address is required")
    private String deviceMac;

    private String deviceName;
}