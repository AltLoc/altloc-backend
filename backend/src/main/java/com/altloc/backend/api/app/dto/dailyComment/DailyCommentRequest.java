package com.altloc.backend.api.app.dto.dailyComment;

import com.altloc.backend.store.enums.Mood;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyCommentRequest {

    private String id;
    private String userId;
    private String content;
    private Mood mood;
}
