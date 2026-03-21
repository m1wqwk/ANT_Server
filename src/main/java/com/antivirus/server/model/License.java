package com.antivirus.server.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "license")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class License {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private LicenseType type;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_activation_date")
    private LocalDateTime firstActivationDate;

    @Column(name = "ending_date")
    private LocalDate endingDate;

    @Column(nullable = false)
    private Boolean blocked = false;

    @Column(name = "device_count", nullable = false)
    private Integer deviceCount = 1;

    private String description;

    private String ipAddress;
}