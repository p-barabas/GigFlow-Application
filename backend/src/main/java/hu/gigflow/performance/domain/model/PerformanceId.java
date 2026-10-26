package hu.gigflow.performance.domain.model;

import java.util.Objects;
import java.util.UUID;

public record PerformanceId(UUID value) {

    public PerformanceId {
        Objects.requireNonNull(value, "A fellépés azonosítója nem lehet null.");
    }

    public static PerformanceId of(UUID value) {
        return new PerformanceId(value);
    }
}
