package com.altloc.backend.store.entities.app;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "identity_matrix")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityMatrixEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "identity_matrix_id", referencedColumnName = "id")
    private List<DomainEntity> domains = new ArrayList<>();

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
