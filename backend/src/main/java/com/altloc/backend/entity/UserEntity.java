package com.altloc.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Data
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
    private boolean emailVerified;

    private String avatarKey; // Может быть null

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int currency;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean isAdmin;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "auth_methods", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private List<AuthMethod> authMethods;

    // Связь с таблицей паролей
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private PasswordEntity passwordAccount;

    // Инициализация createdAt в конструкторе
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public enum AuthMethod {
        PASSWORD, GOOGLE
    }
}
