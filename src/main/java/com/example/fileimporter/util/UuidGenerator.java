package com.example.fileimporter.util;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public final class UuidGenerator {
    private UuidGenerator() {
    }

    public static UUID next() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
