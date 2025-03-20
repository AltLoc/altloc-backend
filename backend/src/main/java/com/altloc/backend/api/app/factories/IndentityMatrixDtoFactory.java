package com.altloc.backend.api.app.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.app.dto.IndentityMatrixDto;
import com.altloc.backend.store.entities.app.IndentityMatrixEntity;

@Component
public class IndentityMatrixDtoFactory {

    public IndentityMatrixDto createIndentityMatrixDto(IndentityMatrixEntity entity) {
        return IndentityMatrixDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .userId(entity.getUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
