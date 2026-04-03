package com.antivirus.server.service;

import com.antivirus.server.DTO.Ticket;
import com.antivirus.server.DTO.TicketResponse;
import com.antivirus.server.model.Device;
import com.antivirus.server.model.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
public class TicketService {

    private final String secret;

    public TicketService(@Value("${ticket.secret}") String secret) {
        this.secret = secret;
    }

    public TicketResponse generate(License license, Device device) {
        return buildSignedTicket(license, device);
    }

    public TicketResponse buildSignedTicket(License license, Device device) {
        Ticket ticket = new Ticket();
        ticket.setServerTime(LocalDateTime.now());
        ticket.setTtl(300L);
        ticket.setActivationDate(license.getFirstActivationDate());
        ticket.setExpiryDate(
                license.getEndingDate() != null
                        ? license.getEndingDate().atStartOfDay()
                        : null
        );
        ticket.setUserId(license.getUser() != null ? license.getUser().getId() : null);
        ticket.setDeviceId(device != null ? device.getId() : null);
        ticket.setBlocked(license.getBlocked());
        ticket.setActivationDate(
                license.getFirstActivationDate() != null
                        ? license.getFirstActivationDate()
                        : null
        );
        ticket.setExpiryDate(
                license.getEndingDate() != null
                        ? license.getEndingDate().atStartOfDay()
                        : null
        );

        String payload = ticket.toString();
        String signature = sign(payload);

        return new TicketResponse(ticket, signature);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(key);
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Signing error", e);
        }
    }
}