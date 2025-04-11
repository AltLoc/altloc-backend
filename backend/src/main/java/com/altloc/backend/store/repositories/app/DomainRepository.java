package com.altloc.backend.store.repositories.app;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.altloc.backend.store.entities.app.DomainEntity;

@Repository
public interface DomainRepository extends JpaRepository<DomainEntity, String> {
    Optional<DomainEntity> findDomainEntityByIdentityMatrixIdAndNameContainsIgnoreCase(
            String identityMatrixId,
            String domainName);

    List<DomainEntity> findAllByUserId(String userId);

    Stream<DomainEntity> streamAllByUserId(String userId);
}
