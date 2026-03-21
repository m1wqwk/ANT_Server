package com.antivirus.server.DTO;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LicenseTicket {
    private String licenseCode;
    private String productName;
    private String licenseType;
    private LocalDateTime firstActivationDate;
    private LocalDate endingDate;
    private Integer deviceCount;
    private Boolean isValid;
    private Integer remainingDays;
    private String status;

    public static LicenseTicket fromLicense(com.antivirus.server.model.License license) {
        boolean isValid = !license.getBlocked() &&
                license.getEndingDate() != null &&
                !license.getEndingDate().isBefore(LocalDate.now());

        long remainingDays = 0;
        if (license.getEndingDate() != null && license.getEndingDate().isAfter(LocalDate.now())) {
            remainingDays = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(), license.getEndingDate()
            );
        }

        String status = license.getBlocked() ? "BLOCKED" :
                (license.getEndingDate() != null && license.getEndingDate().isBefore(LocalDate.now()) ?
                        "EXPIRED" : "ACTIVE");

        return LicenseTicket.builder()
                .licenseCode(license.getCode())
                .productName(license.getProduct().getName())
                .licenseType(license.getType().getName())
                .firstActivationDate(license.getFirstActivationDate())
                .endingDate(license.getEndingDate())
                .deviceCount(license.getDeviceCount())
                .isValid(isValid)
                .remainingDays((int) remainingDays)
                .status(status)
                .build();
    }
}