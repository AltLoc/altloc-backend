package com.altloc.backend.store.repositories.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.altloc.backend.store.entities.auth.GoogleEntity;

public interface GoogleRepository extends JpaRepository<GoogleEntity, String> {

    Optional<GoogleEntity> findByGoogleId(String googleId);

}
