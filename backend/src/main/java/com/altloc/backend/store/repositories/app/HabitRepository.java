package com.altloc.backend.store.repositories.app;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.altloc.backend.store.entities.app.HabitEntity;
import com.altloc.backend.store.enums.DayPart;

@Repository
public interface HabitRepository extends JpaRepository<HabitEntity, String> {
    Optional<HabitEntity> findHabitEntityByDomainIdAndNameContainsIgnoreCase(
            String domainId,
            String habitName);

    @Query("SELECT d.name FROM DomainEntity d WHERE d.id = (SELECT h.domainId FROM HabitEntity h WHERE h.id = :habitId)")
    Optional<String> findDomainNameByHabitId(@Param("habitId") String habitId);

    // Optional<HabitEntity> findHabitsByDayPart(@Param("dayPart") String dayPart);

    List<HabitEntity> findAllByDayPart(DayPart dayPart);

}
