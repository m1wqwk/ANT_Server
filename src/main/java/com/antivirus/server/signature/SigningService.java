package com.antivirus.server.signature;

import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

@Service
public class SigningService {

    private final KeyProvider keyProvider;
    private final CanonicalizationService canonicalizationService;

    public SigningService(KeyProvider keyProvider,
                          CanonicalizationService canonicalizationService) {
        this.keyProvider = keyProvider;
        this.canonicalizationService = canonicalizationService;
    }

    public String sign(Object payload) {
        try {
            byte[] data = canonicalizationService.canonicalize(payload);

            Signature signature = Signature.getInstance("SHA256withRSA");

            signature.initSign(keyProvider.getPrivateKey());
            signature.update(data);

            byte[] signedBytes = signature.sign();

            return Base64.getEncoder().encodeToString(signedBytes);

        } catch (Exception e) {
            throw new RuntimeException("Signing error", e);
        }
    }

    public boolean verify(Object data, String signatureBase64) {
        try {
            byte[] canonical = canonicalizationService.canonicalize(data);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(keyProvider.getPublicKey());
            signature.update(canonical);

            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            return signature.verify(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed", e);
        }
    }

    public byte[] signBytes(byte[] data) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(keyProvider.getPrivateKey());
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
