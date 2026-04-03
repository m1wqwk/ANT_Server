package com.antivirus.server.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    private String licenseCode;
    private Long userId;
    private Long deviceId;
    private LocalDateTime activationDate;
    private LocalDateTime expiryDate;
    private LocalDateTime serverTime;
    private Long ttl;
    private Boolean blocked;
}
