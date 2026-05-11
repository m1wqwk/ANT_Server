package com.antivirus.server.service;

import com.antivirus.server.model.MalwareSignature;
import com.antivirus.server.util.CryptoUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HexFormat;

@Service
public class SignatureCalculationService {

    public MalwareSignature calculate(MultipartFile file, String threatName) {
        try {
            byte[] bytes = file.getBytes();

            int prefixLength = Math.min(32, bytes.length);

            byte[] firstBytes = new byte[prefixLength];
            System.arraycopy(bytes, 0, firstBytes, 0, prefixLength);

            byte[] remainder = new byte[Math.max(0, bytes.length - prefixLength)];

            if (bytes.length > prefixLength) {
                System.arraycopy(
                        bytes,
                        prefixLength,
                        remainder,
                        0,
                        remainder.length
                );
            }

            byte[] hash = CryptoUtils.sha256(remainder);

            MalwareSignature sig = new MalwareSignature();

            sig.setThreatName(threatName);
            sig.setFirstBytesHex(HexFormat.of().formatHex(firstBytes));
            sig.setRemainderHashHex(HexFormat.of().formatHex(hash));
            sig.setRemainderLength((long) remainder.length);
            sig.setFileType(file.getContentType());
            sig.setOffsetStart(0L);
            sig.setOffsetEnd((long) prefixLength);

            return sig;

        } catch (Exception e) {
            throw new RuntimeException("Signature calculation failed", e);
        }
    }
}
