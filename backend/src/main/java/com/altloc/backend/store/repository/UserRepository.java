package com.altloc.backend.store.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.altloc.backend.store.entity.UserEntity;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);

    Boolean existsUserByUsername(String username);

    Boolean existsUserByEmail(String email);
}