package com.antivirus.server.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class SignatureFileUploadResponse {

    private UUID signatureId;
    private String threatName;
    private String objectName;
}
