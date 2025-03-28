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
@Table(name = "domain")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "identity_matrix_id", nullable = false)
    private String identityMatrixId;

    @Column(nullable = false)
    private String name;

    // @ManyToOne
    // @JoinColumn(name = "identity_matrix_id")
    // IdentityMatrixEntity identityMatrix;

    @Builder.Default
    @Column(nullable = false)
    @OneToMany
    @JoinColumn(referencedColumnName = "id")
    private List<HabitEntity> habits = new ArrayList<>();

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
