package com.altloc.backend.store.repositories.app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.altloc.backend.store.entities.app.DomainEntity;

@Repository
public interface DomainRepository extends JpaRepository<DomainEntity, String> {

}
