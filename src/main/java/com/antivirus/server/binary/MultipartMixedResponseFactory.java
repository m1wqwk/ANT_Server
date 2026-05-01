package com.antivirus.server.binary;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class MultipartMixedResponseFactory {

    public static ResponseEntity<MultiValueMap<String, Object>> build(
            byte[] manifest,
            byte[] data) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("manifest", buildPart("manifest.bin", manifest));
        body.add("data", buildPart("data.bin", data));

        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_MIXED)
                .body(body);
    }

    private static HttpEntity<byte[]> buildPart(String filename, byte[] content) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build()
        );

        return new HttpEntity<>(content, headers);
    }
}