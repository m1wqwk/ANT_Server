package com.antivirus.server.binary;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BinaryWriter {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public void writeByte(int value) {
        out.write(value & 0xFF);
    }

    public void writeBytes(byte[] bytes) {
        out.writeBytes(bytes);
    }

    public void writeUint16(int value) {
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    public void writeUint32(long value) {
        out.write((int) ((value >>> 24) & 0xFF));
        out.write((int) ((value >>> 16) & 0xFF));
        out.write((int) ((value >>> 8) & 0xFF));
        out.write((int) (value & 0xFF));
    }

    public void writeInt64(long value) {
        for (int i = 7; i >= 0; i--) {
            out.write((int) ((value >>> (i * 8)) & 0xFF));
        }
    }

    public void writeUUID(UUID uuid) {
        writeInt64(uuid.getMostSignificantBits());
        writeInt64(uuid.getLeastSignificantBits());
    }

    public void writeString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeUint32(bytes.length);
        writeBytes(bytes);
    }

    public void writeByteArray(byte[] bytes) {
        writeUint32(bytes.length);
        writeBytes(bytes);
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }
}