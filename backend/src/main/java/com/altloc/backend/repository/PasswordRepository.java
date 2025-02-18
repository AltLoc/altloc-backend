package com.altloc.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.altloc.backend.entity.PasswordEntity;

public interface PasswordRepository extends JpaRepository<PasswordEntity, String> {
}
