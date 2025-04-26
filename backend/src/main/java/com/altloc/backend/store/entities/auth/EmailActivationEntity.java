package com.altloc.backend.store.entities.auth;

import java.time.LocalDateTime;

import com.altloc.backend.store.entities.UserEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "email_activations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailActivationEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;
}
