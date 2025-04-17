package com.altloc.backend.api.app.dto.habit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompletedHabitRequest {

    private String id;
    private String domainId;
    private String habitId;
    private String userId;
}
