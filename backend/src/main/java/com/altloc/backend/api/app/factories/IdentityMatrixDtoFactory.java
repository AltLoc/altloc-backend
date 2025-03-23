package com.altloc.backend.api.app.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.app.dto.IdentityMatrixDto;
import com.altloc.backend.store.entities.app.IdentityMatrixEntity;

@Component
public class IdentityMatrixDtoFactory {

    public IdentityMatrixDto createIdentityMatrixDto(IdentityMatrixEntity entity) {
        return IdentityMatrixDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .userId(entity.getUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
