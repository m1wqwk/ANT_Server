package com.antivirus.server.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SignatureUrlResponse {

    private UUID signatureId;
    private String url;
}
