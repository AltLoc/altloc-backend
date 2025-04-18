package com.altloc.backend.api.app.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DomainRequest {
    private String id;
    private String identityMatrixId;
    private String name;
}
