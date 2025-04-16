package com.altloc.backend.store.repositories.app;

import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.altloc.backend.store.entities.app.CompletedHabitEntity;

@Repository
public interface CompletedHabitRepository extends JpaRepository<CompletedHabitEntity, String> {

    Optional<CompletedHabitEntity> findFirstByHabitIdAndUserIdAndCompletedAtAfter(
            String habitId, String userId, Instant completedAt);

    @Query("SELECT c.completedAt FROM CompletedHabitEntity c WHERE c.habitId = :habitId")
    List<Instant> findCompletedDatesByHabitId(@Param("habitId") String habitId);
}
