package com.altloc.backend.api.app.factories;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.app.dto.HabitDto;
import com.altloc.backend.store.entities.app.HabitEntity;
import com.altloc.backend.store.repositories.app.CompletedHabitRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class HabitDtoFactory {

    Instant todayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);

    private final CompletedHabitRepository completedHabitRepository;

    public HabitDto createHabitDto(HabitEntity entity) {
        return HabitDto.builder()
                .id(entity.getId())
                .domainId(entity.getDomainId())
                .name(entity.getName())
                .runtime(entity.getRuntime())
                .dayPart(entity.getDayPart())
                .userId(entity.getUserId())
                .isCompleted(
                        completedHabitRepository
                                .findFirstByHabitIdAndUserIdAndCompletedAtAfter(entity.getId(), entity.getUserId(),
                                        todayStart)
                                .isPresent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
