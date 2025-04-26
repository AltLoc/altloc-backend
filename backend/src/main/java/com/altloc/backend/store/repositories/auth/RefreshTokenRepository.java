package com.altloc.backend.store.repositories.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.altloc.backend.store.entities.UserEntity;
import com.altloc.backend.store.entities.auth.RefreshTokenEntity;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByUser(UserEntity user);

    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);

    void deleteByUser(UserEntity user);
}
