package com.antivirus.server.DTO;

import com.antivirus.server.model.SignatureStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class SignatureIncrement {
    private UUID id;
    private SignatureStatus status;
    private Instant updatedAt;
    private String threatName;
}
