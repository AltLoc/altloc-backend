package com.altloc.backend.api.app.dto;

import com.altloc.backend.store.enums.DayPart;

// import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HabitRequest {

    private String id;
    private String domainId;
    private String name;
    private int runtime;
    private DayPart dayPart;
}
