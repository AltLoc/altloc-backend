package com.altloc.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "password_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordEntity {

    // Use user_id as primary key
    @Id
    @Column(name = "user_id")
    private String userId;

    // Connection with UserEntity
    @OneToOne
    @MapsId // Use user_id as primary key
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String password;
}
