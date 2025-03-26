package com.altloc.backend.api.app.factories;

import com.altloc.backend.api.app.dto.HabitDto;
import com.altloc.backend.store.entities.app.HabitEntity;

public class HabitDtoFactory {
    public HabitDto createHabitDto(HabitEntity entity) {
        return HabitDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
