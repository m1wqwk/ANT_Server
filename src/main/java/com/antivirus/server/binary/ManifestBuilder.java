package com.antivirus.server.binary;

import com.antivirus.server.model.MalwareSignature;
import com.antivirus.server.model.SignatureStatus;
import com.antivirus.server.signature.SigningService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class ManifestBuilder {

    private static final String MAGIC = "MF-MANDRYKINA";
    private static final int VERSION = 1;

    private byte[] hexToBytes(String hex) {
        if (hex == null || hex.isBlank()) {
            return new byte[0];
        }

        int len = hex.length();
        byte[] result = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) (
                    (Character.digit(hex.charAt(i), 16) << 4)
                            + Character.digit(hex.charAt(i + 1), 16)
            );
        }

        return result;
    }

    private final SigningService signingService;

    public ManifestBuilder(SigningService signingService) {
        this.signingService = signingService;
    }

    public byte[] build(List<MalwareSignature> records,
                        byte exportType,
                        long since,
                        byte[] dataSha256) {

        BinaryWriter writer = new BinaryWriter();

        writer.writeBytes(MAGIC.getBytes(StandardCharsets.UTF_8));
        writer.writeUint16(VERSION);
        writer.writeByte(exportType);

        writer.writeInt64(System.currentTimeMillis());
        writer.writeInt64(since);

        writer.writeUint32(records.size());
        writer.writeBytes(dataSha256);

        long offset = 0;

        for (MalwareSignature s : records) {

            byte[] recordData = buildRecordData(s);

            byte[] signatureBytes = Base64.getDecoder()
                    .decode(s.getDigitalSignatureBase64());

            writer.writeUUID(s.getId());
            writer.writeByte(mapStatus(s.getStatus()));
            writer.writeInt64(s.getUpdatedAt().toEpochMilli());

            writer.writeUint32(offset);
            writer.writeUint32(recordData.length);

            writer.writeUint32(signatureBytes.length);
            writer.writeBytes(signatureBytes);

            offset += recordData.length;
        }

        byte[] unsigned = writer.toByteArray();

        byte[] signature = signingService.signBytes(unsigned);

        BinaryWriter finalWriter = new BinaryWriter();
        finalWriter.writeBytes(unsigned);
        finalWriter.writeUint32(signature.length);
        finalWriter.writeBytes(signature);

        return finalWriter.toByteArray();
    }

    private byte mapStatus(SignatureStatus status) {
        return switch (status) {
            case ACTUAL -> 1;
            case DELETED -> 2;
        };
    }

    private byte[] buildRecordData(MalwareSignature s) {
        BinaryWriter w = new BinaryWriter();

        w.writeString(s.getThreatName());
        w.writeBytes(hexToBytes(s.getFirstBytesHex()));
        w.writeBytes(hexToBytes(s.getRemainderHashHex()));
        w.writeUint32(s.getRemainderLength());
        w.writeString(s.getFileType());
        w.writeInt64(s.getOffsetStart());
        w.writeInt64(s.getOffsetEnd());

        return w.toByteArray();
    }
}
