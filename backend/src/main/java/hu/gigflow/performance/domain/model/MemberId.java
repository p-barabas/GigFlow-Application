package hu.gigflow.performance.domain.model;

import java.util.Objects;

public record MemberId(String value) {

    public MemberId {
        Objects.requireNonNull(value, "A tag azonosítója nem lehet null.");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("A tag azonosítója nem lehet üres.");
        }
    }

    public static MemberId of(String value) {
        return new MemberId(value);
    }
}
