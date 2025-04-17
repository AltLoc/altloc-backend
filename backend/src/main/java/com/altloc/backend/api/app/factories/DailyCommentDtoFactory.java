package com.altloc.backend.api.app.factories;

import com.altloc.backend.api.app.dto.dailyComment.DailyCommentDto;
import com.altloc.backend.store.entities.app.DailyCommentEntity;
import com.altloc.backend.store.enums.Mood;

public class DailyCommentDtoFactory {

    public DailyCommentDto createDailyCommentDto(DailyCommentEntity entity) {
        return DailyCommentDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .content(entity.getContent())
                .mood(Mood.valueOf(entity.getMood().name()))
                .createdAt(entity.getCreatedAt().toString())
                .updatedAt(entity.getUpdatedAt().toString())
                .build();
    }
}
