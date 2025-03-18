package com.altloc.backend.api.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.dto.CategoryDto;
import com.altloc.backend.store.entities.app.CategoryEntity;

@Component
public class CategoryDtoFactory {
    public CategoryDto createCategoryDto(CategoryEntity entity) {
        return CategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
