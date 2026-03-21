package com.antivirus.server.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class LicenseCreateRequest {
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "License type ID is required")
    @Positive(message = "License type ID must be positive")
    private Long typeId;

    @NotNull(message = "Owner ID is required")
    @Positive(message = "Owner ID must be positive")
    private Long ownerId;

    private Integer deviceCount = 1;
    private String description;
}
