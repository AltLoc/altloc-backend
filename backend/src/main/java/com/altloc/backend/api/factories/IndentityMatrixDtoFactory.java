package com.altloc.backend.api.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.dto.IndentityMatrixDto;
import com.altloc.backend.store.entities.app.IndentityMatrixEntity;

@Component
public class IndentityMatrixDtoFactory {

    public IndentityMatrixDto createIndentityMatrixDto(IndentityMatrixEntity entity) {
        return IndentityMatrixDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
