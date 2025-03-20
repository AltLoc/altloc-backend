package com.altloc.backend.store.repositories.app;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.altloc.backend.store.entities.app.IndentityMatrixEntity;

@Repository
public interface IndentityMatrixRepository extends JpaRepository<IndentityMatrixEntity, String> {

    Optional<IndentityMatrixEntity> findByName(String name);

    Stream<IndentityMatrixEntity> streamAllBy();

    Stream<IndentityMatrixEntity> streamAllByNameStartsWithIgnoreCase(String prefixName);

}
