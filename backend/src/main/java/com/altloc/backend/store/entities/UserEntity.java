package com.altloc.backend.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

import com.altloc.backend.store.entities.auth.PasswordEntity;
import com.altloc.backend.store.enums.Role;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    private String avatarKey;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false)
    @Builder.Default
    private int score = 0;

    @Column(nullable = false)
    @Builder.Default
    private int level = 1;

    @Column(nullable = false)
    @Builder.Default
    private int currency = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Связь с таблицей паролей
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PasswordEntity passwordAccount;

    // Инициализация createdAt
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.role == null) {
            this.role = Role.USER;
        }

    }

    public boolean getEmailVerified() {
        return emailVerified;
    }
}
