package com.altloc.backend.store.entities.app;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

import com.altloc.backend.store.entities.UserEntity;

@Entity
@Table(name = "completed_habit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedHabitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "habit_id", nullable = false)
    private String habitId;

    // @Column(name = "user_id", nullable = false)
    // private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @PrePersist
    public void prePersist() {
        completedAt = Instant.now();
    }

}
