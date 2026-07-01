package com.gitlab.mihnea_purcaru1.service_ticketsense.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "sla_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class SlaSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false, unique = true)
    private LocalDate snapshotDate;

    @Column(name = "compliance_percentage", nullable = false)
    private Double compliancePercentage;

    @Column(name = "active_breaches", nullable = false)
    private Integer activeBreaches;

    @Column(name = "avg_resolution_minutes", nullable = false)
    private Long avgResolutionMinutes;

    public SlaSnapshot(LocalDate snapshotDate, Double compliancePercentage,
                       Integer activeBreaches, Long avgResolutionMinutes) {
        this.snapshotDate = snapshotDate;
        this.compliancePercentage = compliancePercentage;
        this.activeBreaches = activeBreaches;
        this.avgResolutionMinutes = avgResolutionMinutes;
    }
}
