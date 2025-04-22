package com.altloc.backend.api.app.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.app.dto.habit.CompletedHabitDto;
import com.altloc.backend.store.entities.app.CompletedHabitEntity;

@Component
public class CompletedHabitDtoFactory {
    public CompletedHabitDto createCompletedHabitDto(CompletedHabitEntity entity) {
        return CompletedHabitDto.builder()
                .id(entity.getId())
                .habitId(entity.getHabitId())
                .userId(entity.getUser().getId())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
