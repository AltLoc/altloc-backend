package com.altloc.backend.api.app.dto;

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
public class IdentityMatrixDto {

    @NonNull
    private String id;

    @NonNull
    private String userId;

    @NonNull
    private String name;

    @NonNull
    private String description;

    @NonNull
    @JsonProperty("created_at")
    private Instant createdAt;

    @NonNull
    @JsonProperty("updated_at")
    private Instant updatedAt;

}
