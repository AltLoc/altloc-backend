package com.altloc.backend.store.repositories.app;

import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.altloc.backend.store.entities.app.CompletedHabitEntity;

@Repository
public interface CompletedHabitRepository extends JpaRepository<CompletedHabitEntity, String> {

    Optional<CompletedHabitEntity> findFirstByHabitIdAndUserIdAndCompletedAtAfter(
            String habitId, String userId, Instant completedAt);
}
