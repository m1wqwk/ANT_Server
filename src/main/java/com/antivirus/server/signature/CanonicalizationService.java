package com.antivirus.server.signature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class CanonicalizationService {

    private final ObjectMapper mapper;

    public CanonicalizationService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.findAndRegisterModules();

        this.mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        this.mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public byte[] canonicalize(Object payload) {
        try {
            String json = mapper.writeValueAsString(payload);

            String canonicalJson =
                    new org.erdtman.jcs.JsonCanonicalizer(json)
                            .getEncodedString();

            return canonicalJson.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Canonicalization failed", e);
        }
    }
}