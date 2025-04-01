package com.altloc.backend.store.repositories.app;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.altloc.backend.store.entities.app.HabitEntity;

@Repository
public interface HabitRepository extends JpaRepository<HabitEntity, String> {
    Optional<HabitEntity> findHabitEntityByDomainIdAndNameContainsIgnoreCase(
            String domainId,
            String habitName);

}
