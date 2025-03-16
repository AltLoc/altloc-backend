package com.altloc.backend.store.entities.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Date;

import com.altloc.backend.store.entities.UserEntity;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

    @Column(name = "refresh_token", nullable = false, unique = true)
    private String refreshToken;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date expirationDate;
}
