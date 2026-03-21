package com.antivirus.server.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "license_history")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private LicenseStatus status;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    private String description;

    @PrePersist
    protected void onCreate() {
        changeDate = LocalDateTime.now();
    }
}
