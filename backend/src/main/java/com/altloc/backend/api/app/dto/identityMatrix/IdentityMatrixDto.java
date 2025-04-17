package com.altloc.backend.api.app.dto.identityMatrix;

import java.time.Instant;
import java.util.List;

import com.altloc.backend.api.app.dto.domain.DomainDto;

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
    private String bannerKey;

    @NonNull
    private String userId;

    @NonNull
    private String name;

    @NonNull
    private String description;

    @NonNull
    List<DomainDto> domains;

    @NonNull
    private Instant createdAt;

    @NonNull
    private Instant updatedAt;

}
