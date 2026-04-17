package com.antivirus.server.DTO;

public class SignatureRequest {
    public String threatName;
    public String firstBytesHex;
    public String remainderHashHex;
    public Long remainderLength;
    public String fileType;
    public Long offsetStart;
    public Long offsetEnd;
    public String status;
}
