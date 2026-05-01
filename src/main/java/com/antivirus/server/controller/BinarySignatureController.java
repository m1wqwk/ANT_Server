package com.antivirus.server.controller;

import com.antivirus.server.binary.DataBinBuilder;
import com.antivirus.server.binary.ManifestBuilder;
import com.antivirus.server.binary.MultipartMixedResponseFactory;
import com.antivirus.server.model.MalwareSignature;
import com.antivirus.server.model.SignatureStatus;
import com.antivirus.server.repository.MalwareSignatureRepository;
import com.antivirus.server.util.CryptoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/binary/signatures")
public class BinarySignatureController {

    private final MalwareSignatureRepository repo;
    private final DataBinBuilder dataBuilder;
    private final ManifestBuilder manifestBuilder;


    @GetMapping("/full")
    public ResponseEntity<?> full() {

        List<MalwareSignature> records =
                repo.findByStatus(SignatureStatus.ACTUAL);

        return buildResponse(records, (byte)1, -1);
    }

    @GetMapping("/increment")
    public ResponseEntity<?> increment(@RequestParam Long since) {

        if (since == null) {
            return ResponseEntity.badRequest().build();
        }

        List<MalwareSignature> records =
                repo.findByStatus(SignatureStatus.ACTUAL);

        return buildResponse(records, (byte)2, since);
    }

    @PostMapping("/by-ids")
    public ResponseEntity<?> byIds(@RequestBody List<UUID> ids) {

        List<MalwareSignature> records = repo.findAllById(ids);

        return buildResponse(records, (byte)3, -1);
    }

    private ResponseEntity<?> buildResponse(List<MalwareSignature> records,
                                            byte exportType,
                                            long since) {

        byte[] data = dataBuilder.build(records);

        byte[] sha256 = CryptoUtils.sha256(data);

        byte[] manifest = manifestBuilder.build(
                records,
                exportType,
                since,
                sha256
        );

        return MultipartMixedResponseFactory.build(manifest, data);
    }
}
