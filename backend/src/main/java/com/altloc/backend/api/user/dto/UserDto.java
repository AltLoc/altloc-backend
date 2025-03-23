package com.altloc.backend.api.user.dto;

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
public class UserDto {

    @NonNull
    private String id;

    @NonNull
    private String username;

    @NonNull
    private String email;

    private boolean emailVerified;

    private String avatarKey;

    private int score;

    private int level;

    private int currency;

    @NonNull
    @JsonProperty("created_at")
    private Instant createdAt;

}
