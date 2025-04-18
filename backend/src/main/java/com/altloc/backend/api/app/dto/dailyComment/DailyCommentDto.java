package com.altloc.backend.api.app.dto.dailyComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCommentDto {

    @NonNull
    private String id;

    @NonNull
    private String userId;

    @NonNull
    private String content;

    @NonNull
    private MoodDto mood;

    @NonNull
    private String createdAt;

    @NonNull
    private String updatedAt;

}
