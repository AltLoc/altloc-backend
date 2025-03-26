package com.altloc.backend.api.app.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.app.dto.DomainDto;
import com.altloc.backend.store.entities.app.DomainEntity;

@Component
public class DomainDtoFactory {
    public DomainDto createDomainDto(DomainEntity entity) {
        return DomainDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
