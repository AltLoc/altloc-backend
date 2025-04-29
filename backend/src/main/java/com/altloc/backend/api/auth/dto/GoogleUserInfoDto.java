package com.altloc.backend.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfoDto {

    @NonNull
    private String sub;

    @NonNull
    private String name;

    @NonNull
    private String picture;

    @NonNull
    private String email;

    @NonNull
    private Boolean emailVerified;

    @NonNull
    private String locale;
}
