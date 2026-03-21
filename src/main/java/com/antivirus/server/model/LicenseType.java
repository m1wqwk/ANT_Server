package com.antivirus.server.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@Table(name = "license_type")
@NoArgsConstructor
@AllArgsConstructor
public class LicenseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "default_duration_in_days", nullable = false)
    private Integer defaultDurationInDays;

    private String description;
}