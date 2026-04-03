package com.antivirus.server.service;

import com.antivirus.server.DTO.*;
import com.antivirus.server.model.*;
import com.antivirus.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final ProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;
    private final TicketService ticketService;

    private static final String LICENSE_CODE_PREFIX = "ANT-";
    private static final int LICENSE_CODE_LENGTH = 16;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public License createLicense(LicenseCreateRequest request, Long adminId) {
        log.info("Creating new license by admin: {}", adminId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + request.getProductId()));

        LicenseType licenseType = licenseTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("License type not found with id: " + request.getTypeId()));

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + request.getOwnerId()));

        if (!owner.getEnabled()) {
            throw new IllegalArgumentException("Owner account is disabled");
        }

        String licenseCode = generateUniqueLicenseCode();

        License license = License.builder()
                .code(licenseCode)
                .product(product)
                .type(licenseType)
                .owner(owner)
                .user(null)
                .firstActivationDate(null)
                .endingDate(null)
                .blocked(false)
                .deviceCount(request.getDeviceCount() != null ? request.getDeviceCount() : 1)
                .description(request.getDescription())
                .build();

        License savedLicense = licenseRepository.save(license);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        LicenseHistory history = LicenseHistory.builder()
                .license(license)
                .user(admin)
                .status(LicenseStatus.CREATED)
                .changeDate(LocalDateTime.now())
                .build();

        licenseHistoryRepository.save(history);

        log.info("License created successfully: {} by admin: {}", licenseCode, adminId);
        return savedLicense;
    }

    @Transactional
    public TicketResponse activateLicense(LicenseActivateRequest request, Long userId) {
        log.info("Activating license: {} for user: {}", request.getActivationKey(), userId);

        License license = licenseRepository.findByCodeForUpdate(request.getActivationKey())
                .orElseThrow(() -> new IllegalArgumentException("License not found with code: " + request.getActivationKey()));

        if (license.getProduct().getIsBlocked()) {
            throw new IllegalStateException("Product is blocked");
        }

        if (license.getBlocked()) {
            throw new IllegalStateException("License is blocked");
        }

        if (license.getEndingDate() != null &&
                license.getEndingDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("License expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (license.getUser() != null && !license.getUser().getId().equals(userId)) {
            throw new SecurityException("License already activated by another user");
        }

        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseGet(() -> {
                    log.info("Creating new device with MAC: {} for user: {}", request.getDeviceMac(), userId);

                    String deviceId = UUID.randomUUID().toString();

                    Device newDevice = Device.builder()
                            .deviceId(deviceId)
                            .macAddress(request.getDeviceMac())
                            .name(request.getDeviceName() != null ? request.getDeviceName() : "Unknown Device")
                            .user(user)
                            .build();
                    return deviceRepository.save(newDevice);
                });

        if (!device.getUser().getId().equals(userId)) {
            throw new SecurityException("Device belongs to another user");
        }

        if (deviceLicenseRepository.findByLicenseAndDevice(license, device).isPresent()) {
            throw new IllegalStateException("License already activated on this device");
        }

        if (license.getUser() == null) {
            return activateFirstTime(license, user, device);
        } else {
            return activateAdditionalDevice(license, user, device);
        }
    }

    private TicketResponse activateFirstTime(License license, User user, Device device) {
        log.info("First activation for license: {}", license.getCode());

        license.setUser(user);

        LocalDateTime now = LocalDateTime.now();
        license.setFirstActivationDate(now);

        LocalDate endingDate = now.toLocalDate().plusDays(license.getType().getDefaultDurationInDays());
        license.setEndingDate(endingDate);

        License savedLicense = licenseRepository.save(license);

        DeviceLicense deviceLicense = DeviceLicense.builder()
                .license(savedLicense)
                .device(device)
                .build();
        deviceLicenseRepository.save(deviceLicense);

        LicenseHistory history = LicenseHistory.builder()
                .license(savedLicense)
                .user(user)
                .status(LicenseStatus.ACTIVATED)
                .description(String.format("License activated by user: %s on device: %s (MAC: %s)",
                        user.getUsername(), device.getName(), device.getMacAddress()))
                .build();
        licenseHistoryRepository.save(history);

        log.info("License activated successfully (first time): {} for user: {}", license.getCode(), user.getUsername());
        return ticketService.generate(license, device);
    }

    private TicketResponse activateAdditionalDevice(License license, User user, Device device) {
        log.info("Additional activation for license: {} on device: {}", license.getCode(), device.getMacAddress());

        long activeDevicesCount = deviceLicenseRepository.countByLicense(license);

        if (activeDevicesCount >= license.getDeviceCount()) {
            throw new IllegalStateException(String.format(
                    "Device limit reached. Current devices: %d, Limit: %d",
                    activeDevicesCount, license.getDeviceCount()));
        }

        DeviceLicense deviceLicense = DeviceLicense.builder()
                .license(license)
                .device(device)
                .build();
        deviceLicenseRepository.save(deviceLicense);

        LicenseHistory history = LicenseHistory.builder()
                .license(license)
                .user(user)
                .status(LicenseStatus.ACTIVATED)
                .description(String.format("License activated on additional device: %s (MAC: %s)",
                        device.getName(), device.getMacAddress()))
                .build();
        licenseHistoryRepository.save(history);

        log.info("License activated on additional device: {} for license: {}", device.getMacAddress(), license.getCode());
        return ticketService.generate(license, device);
    }

    @Transactional
    public TicketResponse renewLicense(LicenseRenewRequest request, Long userId) {
        log.info("Renewing license: {} for user: {}", request.getActivationKey(), userId);

        // Поиск лицензии по коду
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new IllegalArgumentException("License not found with code: " + request.getActivationKey()));

        // Проверка принадлежности лицензии пользователю
        if (license.getUser() == null || !license.getUser().getId().equals(userId)) {
            throw new SecurityException("License does not belong to this user");
        }

        // Проверка возможности продления
        boolean canRenew = checkRenewability(license);
        if (!canRenew) {
            throw new IllegalStateException("License cannot be renewed. License must be active or expired within 7 days");
        }

        if (license.getEndingDate() == null) {
            throw new IllegalStateException("License was never activated");
        }

        LocalDate currentEndingDate = license.getEndingDate();
        LocalDate newEndingDate = currentEndingDate.plusDays(license.getType().getDefaultDurationInDays());
        license.setEndingDate(newEndingDate);

        License savedLicense = licenseRepository.save(license);

        // Создание записи в истории
        User user = userRepository.findById(userId).get();
        LicenseHistory history = LicenseHistory.builder()
                .license(savedLicense)
                .user(user)
                .status(LicenseStatus.RENEWED)
                .description(String.format("License renewed. Extended from %s to %s (+%d days)",
                        currentEndingDate, newEndingDate, license.getType().getDefaultDurationInDays()))
                .build();
        licenseHistoryRepository.save(history);

        log.info("License renewed successfully: {} for user: {}", license.getCode(), user.getUsername());
        return ticketService.generate(license, null);
    }

    private boolean checkRenewability(License license) {
        if (license.getBlocked()) {
            return false;
        }

        LocalDate now = LocalDate.now();
        LocalDate endingDate = license.getEndingDate();

        if (endingDate.isAfter(now)) {
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, endingDate);
            return daysRemaining <= 7;
        }

        long daysExpired = java.time.temporal.ChronoUnit.DAYS.between(endingDate, now);
        return daysExpired <= 7;
    }

    @Transactional(readOnly = true)
    public TicketResponse checkLicense(LicenseCheckRequest request, Long userId) {
        log.info("Checking license for user: {}, device: {}, product: {}",
                userId, request.getDeviceMac(), request.getProductId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + request.getProductId()));

        if (product.getIsBlocked()) {
            throw new IllegalStateException("Product is blocked");
        }

        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseThrow(() -> new IllegalArgumentException("Device not found with MAC: " + request.getDeviceMac()));

        if (!device.getUser().getId().equals(userId)) {
            throw new SecurityException("Device belongs to another user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        License license = licenseRepository.findActiveByDeviceUserAndProduct(
                        user, product, request.getDeviceMac(), LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException("No active license found for this product on this device"));

        log.info("License check successful for user: {}, product: {}, remaining days: {}",
                user.getUsername(), product.getName(),
                java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), license.getEndingDate()));

        return ticketService.generate(license, device);
    }

    private String generateUniqueLicenseCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (licenseRepository.findByCode(code).isPresent());
        return code;
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(LICENSE_CODE_PREFIX);

        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                code.append("-");
            }
            code.append(generateRandomSegment(4));
        }

        return code.toString();
    }

    private String generateRandomSegment(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder segment = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            segment.append(characters.charAt(secureRandom.nextInt(characters.length())));
        }

        return segment.toString();
    }

    @Transactional
    public void blockLicense(Long licenseId, Long adminId, String reason) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("License not found with id: " + licenseId));

        if (license.getBlocked()) {
            throw new IllegalStateException("License is already blocked");
        }

        license.setBlocked(true);
        licenseRepository.save(license);

        User admin = userRepository.findById(adminId).get();
        LicenseHistory history = LicenseHistory.builder()
                .license(license)
                .user(admin)
                .status(LicenseStatus.BLOCKED)
                .description(reason != null ? reason : "License blocked by administrator")
                .build();
        licenseHistoryRepository.save(history);

        log.info("License blocked: {} by admin: {}, reason: {}", license.getCode(), adminId, reason);
    }

    @Transactional
    public void unblockLicense(Long licenseId, Long adminId, String reason) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("License not found with id: " + licenseId));

        if (!license.getBlocked()) {
            throw new IllegalStateException("License is not blocked");
        }

        license.setBlocked(false);
        licenseRepository.save(license);

        User admin = userRepository.findById(adminId).get();
        LicenseHistory history = LicenseHistory.builder()
                .license(license)
                .user(admin)
                .status(LicenseStatus.UNBLOCKED)
                .description(reason != null ? reason : "License unblocked by administrator")
                .build();
        licenseHistoryRepository.save(history);

        log.info("License unblocked: {} by admin: {}, reason: {}", license.getCode(), adminId, reason);
    }

    public List<LicenseHistory> getLicenseHistory(Long licenseId) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("License not found with id: " + licenseId));

        return licenseHistoryRepository.findByLicenseOrderByChangeDateDesc(license);
    }

    public List<License> getUserLicenses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return licenseRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<License> getAllLicenses() {
        log.info("Getting all licenses");
        return licenseRepository.findAll();
    }
}