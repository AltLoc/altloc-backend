package com.altloc.backend.api.app.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.app.dto.dailyComment.DailyCommentDto;
import com.altloc.backend.api.app.dto.dailyComment.MoodDto;
import com.altloc.backend.store.entities.app.DailyCommentEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class DailyCommentDtoFactory {

    public DailyCommentDto createDailyCommentDto(DailyCommentEntity entity) {
        return DailyCommentDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .content(entity.getContent())
                .mood(MoodDto.builder()
                        .label(entity.getMood().getLabel())
                        .emoji(entity.getMood().getEmoji())
                        .build())
                .createdAt(entity.getCreatedAt().toString())
                .updatedAt(entity.getUpdatedAt().toString())
                .build();
    }
}
