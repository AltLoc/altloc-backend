package com.altloc.backend.api.app.dto.habit;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedHabitDto {

    @NonNull
    private String id;

    @NonNull
    protected String habitId;

    @NonNull
    protected String userId;

    @NonNull
    @JsonProperty("completed_at")
    private Instant completedAt;

}
