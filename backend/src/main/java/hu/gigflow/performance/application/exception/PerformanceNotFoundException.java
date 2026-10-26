package hu.gigflow.performance.application.exception;

import hu.gigflow.performance.domain.model.PerformanceId;

public final class PerformanceNotFoundException extends RuntimeException {

    private final PerformanceId performanceId;

    public PerformanceNotFoundException(PerformanceId performanceId) {
        super("A fellépés nem található: " + performanceId.value());
        this.performanceId = performanceId;
    }

    public PerformanceId performanceId() {
        return performanceId;
    }
}
