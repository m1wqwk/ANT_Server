package com.antivirus.server.service;

import com.antivirus.server.DTO.Ticket;
import com.antivirus.server.DTO.TicketResponse;
import com.antivirus.server.model.Device;
import com.antivirus.server.model.License;
import com.antivirus.server.signature.SigningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class TicketService {

    private final SigningService signingService;

    public TicketService(SigningService signingService) {
        this.signingService = signingService;
    }

    public TicketResponse generate(License license, Device device) {
        TicketResponse ticketResponse = buildSignedTicket(license, device);

        log.info("Generated ticket: {}, signature: {}",
                ticketResponse.getTicket().getTicketId(),
                ticketResponse.getSignature());

        return ticketResponse;
    }

    private TicketResponse buildSignedTicket(License license, Device device) {
        Ticket ticket = new Ticket();

        ticket.setTicketId(UUID.randomUUID().toString());
        ticket.setServerTime(LocalDateTime.now());
        ticket.setTtl(300L); // 5 минут

        ticket.setActivationDate(license.getFirstActivationDate());

        ticket.setExpiryDate(
                license.getEndingDate() != null
                        ? license.getEndingDate().atStartOfDay()
                        : null
        );

        ticket.setUserId(
                license.getUser() != null ? license.getUser().getId() : null
        );

        ticket.setDeviceId(
                device != null ? device.getId() : null
        );

        ticket.setBlocked(license.getBlocked());

        String signature = signingService.sign(ticket);

        return new TicketResponse(ticket, signature);
    }

    public boolean verifyTicket(TicketResponse ticketResponse) {
        boolean valid = signingService.verify(ticketResponse.getTicket(), ticketResponse.getSignature());
        log.info("Ticket verification for {}: {}", ticketResponse.getTicket().getTicketId(), valid);
        return valid;
    }

    public boolean isTicketValid(Ticket ticket) {
        if (ticket.getServerTime() == null || ticket.getTtl() == null) return false;
        boolean valid = LocalDateTime.now().isBefore(ticket.getServerTime().plusSeconds(ticket.getTtl()));
        log.info("Ticket TTL check for {}: {}", ticket.getTicketId(), valid);
        return valid;
    }
}