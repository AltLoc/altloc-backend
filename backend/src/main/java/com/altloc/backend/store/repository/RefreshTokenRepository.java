package com.altloc.backend.store.repository;

import com.altloc.backend.store.entity.RefreshTokenEntity;
import com.altloc.backend.store.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByUser(UserEntity user);

    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);

    void deleteByUser(UserEntity user);
}
