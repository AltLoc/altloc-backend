package com.altloc.backend.store.entities.app;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

import com.altloc.backend.store.enums.DayPart;

@Entity
@Table(name = "habit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "domain_id", nullable = false)
    private String domainId;

    // @ManyToOne
    // @JoinColumn(name = "domain_id", referencedColumnName = "id", insertable =
    // false, updatable = false)
    // private DomainEntity domain;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int runtime;

    @Column(nullable = false)
    private int targetNumberOfCompletions;

    @Column(nullable = false)
    private int numberOfCompletions;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_part", nullable = false)
    private DayPart dayPart;

    // @ManyToOne
    // @JoinColumn(name = "domain_id", nullable = false)
    // private DomainEntity domain;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

}
