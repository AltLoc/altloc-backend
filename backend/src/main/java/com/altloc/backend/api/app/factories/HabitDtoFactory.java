package com.altloc.backend.api.app.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.app.dto.HabitDto;
import com.altloc.backend.store.entities.app.HabitEntity;

@Component
public class HabitDtoFactory {
    public HabitDto createHabitDto(HabitEntity entity) {
        return HabitDto.builder()
                .id(entity.getId())
                .domainId(entity.getDomainId())
                .name(entity.getName())
                .runtime(entity.getRuntime())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
