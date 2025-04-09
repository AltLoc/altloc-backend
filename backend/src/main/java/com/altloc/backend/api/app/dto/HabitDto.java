package com.altloc.backend.api.app.dto;

import java.time.Instant;

import com.altloc.backend.store.enums.DayPart;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitDto {

    @NonNull
    private String id;

    @NonNull
    protected String domainId;

    @NonNull
    protected String userId;

    @NonNull
    private String name;

    @NonNull
    private String domainName;

    @NotBlank
    private int runtime;

    @NotBlank
    private DayPart dayPart;

    @NotBlank
    private int targetNumberOfCompletions;

    @NotBlank
    private int numberOfCompletions;

    @NotBlank
    private Boolean isCompleted;

    @NonNull
    @JsonProperty("created_at")
    private Instant createdAt;

    @NonNull
    @JsonProperty("updated_at")
    private Instant updatedAt;
}
