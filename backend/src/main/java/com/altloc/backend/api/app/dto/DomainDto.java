package com.altloc.backend.api.app.dto;

import java.time.Instant;
import java.util.List;

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
    protected String identityMatrixId;

    @NonNull
    protected String userId;

    @NonNull
    private String name;

    @NonNull
    List<HabitDto> habits;

    @NonNull
    private Instant createdAt;

    @NonNull
    private Instant updatedAt;
}
