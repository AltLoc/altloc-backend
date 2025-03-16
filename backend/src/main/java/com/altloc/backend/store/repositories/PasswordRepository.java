package com.altloc.backend.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.altloc.backend.store.entities.auth.PasswordEntity;

public interface PasswordRepository extends JpaRepository<PasswordEntity, String> {
}
