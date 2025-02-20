package com.altloc.backend.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.altloc.backend.store.entity.PasswordEntity;

public interface PasswordRepository extends JpaRepository<PasswordEntity, String> {
}
