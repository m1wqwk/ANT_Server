package com.antivirus.server.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LicenseRenewRequest {
    @NotBlank(message = "Activation key is required")
    private String activationKey;
}
