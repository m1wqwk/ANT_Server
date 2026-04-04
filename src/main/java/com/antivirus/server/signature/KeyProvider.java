package com.antivirus.server.signature;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

@Service
public class KeyProvider {

    private final SignatureProperties props;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public KeyProvider(SignatureProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        try {
            KeyStore keyStore = KeyStore.getInstance(props.getKeyStoreType());

            InputStream is = loadKeyStore();

            keyStore.load(is, props.getKeyStorePassword().toCharArray());

            Key key = keyStore.getKey(
                    props.getKeyAlias(),
                    props.getKeyPassword().toCharArray()
            );

            if (!(key instanceof PrivateKey)) {
                throw new RuntimeException("Not a private key");
            }

            this.privateKey = (PrivateKey) key;

            Certificate cert = keyStore.getCertificate(props.getKeyAlias());
            this.publicKey = cert.getPublicKey();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load keystore", e);
        }
    }

    private InputStream loadKeyStore() throws IOException {
        if (props.getKeyStorePath().startsWith("classpath:")) {
            String path = props.getKeyStorePath().replace("classpath:", "");
            return getClass().getClassLoader().getResourceAsStream(path);
        } else if (props.getKeyStorePath().startsWith("file:")) {
            return new FileInputStream(props.getKeyStorePath().replace("file:", ""));
        } else {
            return new FileInputStream(props.getKeyStorePath());
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
