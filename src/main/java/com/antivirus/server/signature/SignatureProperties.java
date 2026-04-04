package com.antivirus.server.signature;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "signature")
@Getter
@Setter
public class SignatureProperties {

    private String keyStorePath;
    private String keyStoreType;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;
}