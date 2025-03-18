package com.altloc.backend.api.factories;

import com.altloc.backend.api.dto.TaskDto;
import com.altloc.backend.store.entities.app.TaskEntity;

public class TaskDtoFactory {
    public TaskDto createTaskDto(TaskEntity entity) {
        return TaskDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
