package com.antivirus.server.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String deviceId; // optional
}