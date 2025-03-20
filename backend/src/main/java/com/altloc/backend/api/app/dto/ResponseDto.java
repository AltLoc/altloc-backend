package com.altloc.backend.api.app.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto {
    private Boolean answer;

    public static ResponseDto makeDefault(Boolean answer) {
        return builder()
                .answer(answer)
                .build();
    }
}
