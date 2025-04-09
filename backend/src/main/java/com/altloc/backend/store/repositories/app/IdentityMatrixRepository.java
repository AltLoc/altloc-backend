package com.altloc.backend.store.repositories.app;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.altloc.backend.store.entities.app.IdentityMatrixEntity;

@Repository
public interface IdentityMatrixRepository extends JpaRepository<IdentityMatrixEntity, String> {

    Optional<IdentityMatrixEntity> findByName(String name);

    Stream<IdentityMatrixEntity> streamAllBy();

    Stream<IdentityMatrixEntity> streamAllByNameStartsWithIgnoreCase(String prefixName);

    boolean existsByUserId(String userId);

}
