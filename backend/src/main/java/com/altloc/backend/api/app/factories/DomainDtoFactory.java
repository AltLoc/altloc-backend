package com.altloc.backend.api.app.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.app.dto.domain.DomainDto;
import com.altloc.backend.store.entities.app.DomainEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class DomainDtoFactory {

    private final HabitDtoFactory habitDtoFactory;

    public DomainDto createDomainDto(DomainEntity entity) {
        return DomainDto.builder()
                .id(entity.getId())
                .identityMatrixId(entity.getIdentityMatrixId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .habits(entity.getHabits()
                        .stream()
                        .map(habitDtoFactory::createHabitDto)
                        .toList())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
