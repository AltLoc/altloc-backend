package com.altloc.backend.store.repositories.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.altloc.backend.store.entities.auth.EmailActivationEntity;

public interface EmailActivationRepository extends JpaRepository<EmailActivationEntity, String> {

    Optional<EmailActivationEntity> findByToken(String token);
}
