package com.altloc.backend.api.app.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.app.dto.identityMatrix.IdentityMatrixDto;
import com.altloc.backend.store.entities.app.IdentityMatrixEntity;

@Component
public class IdentityMatrixDtoFactory {

    private final DomainDtoFactory domainDtoFactory;

    IdentityMatrixDtoFactory(DomainDtoFactory domainDtoFactory) {
        this.domainDtoFactory = domainDtoFactory;
    }

    public IdentityMatrixDto createIdentityMatrixDto(IdentityMatrixEntity entity) {
        return IdentityMatrixDto.builder()
                .id(entity.getId())
                .bannerKey(entity.getBannerKey())
                .name(entity.getName())
                .description(entity.getDescription())
                .userId(entity.getUserId())
                .domains(entity.getDomains()
                        .stream()
                        .map(domainDtoFactory::createDomainDto)
                        .toList())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
