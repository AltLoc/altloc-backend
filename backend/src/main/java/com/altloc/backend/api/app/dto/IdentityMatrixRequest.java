package com.altloc.backend.api.app.dto;

// import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdentityMatrixRequest {

    private String id;

    // @NotBlank
    private String name;

    private String description;

}
