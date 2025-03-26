package com.altloc.backend.api.app.dto;

import java.time.Instant;
import java.util.List;

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
public class DomainDto {

    @NonNull
    private String id;

    @NonNull
    protected String matrixId;

    @NonNull
    private String name;

    @NonNull
    List<HabitDto> habits;

    @NonNull
    @JsonProperty("created_at")
    private Instant createdAt;

    @NonNull
    @JsonProperty("updated_at")
    private Instant updatedAt;
}
