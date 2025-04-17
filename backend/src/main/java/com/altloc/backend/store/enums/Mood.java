package com.altloc.backend.store.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Mood {
    VERY_BAD("Very bad", "😡"),
    BAD("Bad", "😔"),
    NEUTRAL("Neutral", "😐"),
    GOOD("Good", "😊"),
    VERY_GOOD("Very good", "🤩");

    private final String label;
    private final String emoji;

    @Override
    public String toString() {
        return label;
    }
}
