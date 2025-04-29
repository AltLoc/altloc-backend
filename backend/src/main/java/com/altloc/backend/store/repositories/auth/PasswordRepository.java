package com.altloc.backend.store.repositories.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.altloc.backend.store.entities.auth.PasswordEntity;

@Repository
public interface PasswordRepository extends JpaRepository<PasswordEntity, String> {
}
