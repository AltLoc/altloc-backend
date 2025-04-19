package com.altloc.backend.api.app.dto.dailyComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodDto {
    private String label;
    private String emoji;
    private int score;
}
