package com.antivirus.server.DTO;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SignatureUrlRequest {
    private List<UUID> signatureIds;
}