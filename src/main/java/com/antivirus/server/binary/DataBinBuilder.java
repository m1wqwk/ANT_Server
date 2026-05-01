package com.antivirus.server.binary;

import com.antivirus.server.model.MalwareSignature;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class DataBinBuilder {

    private static final String MAGIC = "DB-MANDRYKINA";
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

    public byte[] build(List<MalwareSignature> records) {
        BinaryWriter writer = new BinaryWriter();

        writer.writeBytes(MAGIC.getBytes(StandardCharsets.UTF_8));
        writer.writeUint16(VERSION);
        writer.writeUint32(records.size());

        for (MalwareSignature s : records) {

            writer.writeString(s.getThreatName());

            writer.writeBytes(hexToBytes(s.getFirstBytesHex()));
            writer.writeBytes(hexToBytes(s.getRemainderHashHex()));

            writer.writeUint32(s.getRemainderLength());

            writer.writeString(s.getFileType());

            writer.writeInt64(s.getOffsetStart());
            writer.writeInt64(s.getOffsetEnd());
        }

        return writer.toByteArray();
    }
}
