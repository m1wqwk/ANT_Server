package com.antivirus.server.controller;

import com.antivirus.server.DTO.*;
import com.antivirus.server.model.License;
import com.antivirus.server.model.LicenseHistory;
import com.antivirus.server.model.User;
import com.antivirus.server.repository.UserRepository;
import com.antivirus.server.service.LicenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;
    private final UserRepository userRepository;


    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createLicense(@Valid @RequestBody LicenseCreateRequest request,
                                           Authentication authentication) {
        try {
            Long adminId = getUserIdFromAuthentication(authentication);
            License license = licenseService.createLicense(request, adminId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "License created successfully",
                            "licenseCode", license.getCode(),
                            "license", license
                    ));
        } catch (IllegalArgumentException e) {
            log.warn("License creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during license creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/activate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> activateLicense(@Valid @RequestBody LicenseActivateRequest request,
                                             Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            TicketResponse ticket = licenseService.activateLicense(request, userId);

            return ResponseEntity.ok(Map.of(
                    "message", "License activated successfully",
                    "ticket", ticket
            ));
        } catch (IllegalArgumentException e) {
            log.warn("License activation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            log.warn("Security error during license activation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Business error during license activation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during license activation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/renew")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> renewLicense(@Valid @RequestBody LicenseRenewRequest request,
                                          Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            TicketResponse ticket = licenseService.renewLicense(request, userId);

            return ResponseEntity.ok(Map.of(
                    "message", "License renewed successfully",
                    "ticket", ticket
            ));
        } catch (IllegalArgumentException e) {
            log.warn("License renewal failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            log.warn("Security error during license renewal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Business error during license renewal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during license renewal", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkLicense(@Valid @RequestBody LicenseCheckRequest request,
                                          Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            TicketResponse ticket = licenseService.checkLicense(request, userId);

            return ResponseEntity.ok(Map.of(
                    "message", "License is valid",
                    "ticket", ticket
            ));
        } catch (IllegalArgumentException e) {
            log.warn("License check failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            log.warn("Security error during license check: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during license check", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/{licenseId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockLicense(@PathVariable Long licenseId,
                                          @RequestParam(required = false) String reason,
                                          Authentication authentication) {
        try {
            Long adminId = getUserIdFromAuthentication(authentication);
            licenseService.blockLicense(licenseId, adminId, reason);

            return ResponseEntity.ok(Map.of(
                    "message", "License blocked successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.warn("License block failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Business error during license block: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during license block", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/{licenseId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unblockLicense(@PathVariable Long licenseId,
                                            @RequestParam(required = false) String reason,
                                            Authentication authentication) {
        try {
            Long adminId = getUserIdFromAuthentication(authentication);
            licenseService.unblockLicense(licenseId, adminId, reason);

            return ResponseEntity.ok(Map.of(
                    "message", "License unblocked successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.warn("License unblock failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Business error during license unblock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during license unblock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{licenseId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLicenseHistory(@PathVariable Long licenseId) {
        try {
            List<LicenseHistory> history = licenseService.getLicenseHistory(licenseId);

            return ResponseEntity.ok(Map.of(
                    "licenseId", licenseId,
                    "history", history
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Get license history failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during get license history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/my-licenses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserLicenses(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            List<License> licenses = licenseService.getUserLicenses(userId);

            return ResponseEntity.ok(Map.of(
                    "licenses", licenses,
                    "count", licenses.size()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Get user licenses failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during get user licenses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        } else if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            User foundUser = userRepository.findByUsername(springUser.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return foundUser.getId();
        } else {
            throw new SecurityException("User not authenticated properly");
        }
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllLicenses() {
        try {
            List<License> licenses = licenseService.getAllLicenses();
            return ResponseEntity.ok(Map.of(
                    "licenses", licenses,
                    "count", licenses.size()
            ));
        } catch (Exception e) {
            log.error("Error getting all licenses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
}