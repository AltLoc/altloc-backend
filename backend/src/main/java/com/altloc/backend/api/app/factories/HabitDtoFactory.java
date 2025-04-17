package com.altloc.backend.api.app.factories;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.app.dto.habit.HabitDto;
import com.altloc.backend.store.entities.app.HabitEntity;
import com.altloc.backend.store.repositories.app.CompletedHabitRepository;
import com.altloc.backend.store.repositories.app.HabitRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class HabitDtoFactory {

    Instant todayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);

    private final CompletedHabitRepository completedHabitRepository;
    private final HabitRepository habitRepository;

    public HabitDto createHabitDto(HabitEntity entity) {
        return HabitDto.builder()
                .id(entity.getId())
                .domainId(entity.getDomainId())
                .name(entity.getName())
                .domainName(
                        habitRepository
                                .findDomainNameByHabitId(entity.getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Domain ID not found")))
                .runtime(entity.getRuntime())
                .dayPart(entity.getDayPart())
                .userId(entity.getUserId())
                .isCompleted(
                        completedHabitRepository
                                .findFirstByHabitIdAndUserIdAndCompletedAtAfter(
                                        entity.getId(), entity.getUserId(),
                                        todayStart)
                                .isPresent())
                .completedDates(
                        completedHabitRepository.findCompletedDatesByHabitId(entity.getId()))
                .targetNumberOfCompletions(entity.getTargetNumberOfCompletions())
                .numberOfCompletions(entity.getNumberOfCompletions())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
