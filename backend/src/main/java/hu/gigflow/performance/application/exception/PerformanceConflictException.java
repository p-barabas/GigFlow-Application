package hu.gigflow.performance.application.exception;

import hu.gigflow.performance.domain.model.PerformanceId;

import java.util.Objects;

public final class PerformanceConflictException extends RuntimeException {

    private final PerformanceId conflictingPerformanceId;

    public PerformanceConflictException(PerformanceId conflictingPerformanceId) {
        super("A fellépés azonos címmel, helyszínnel és kezdési időponttal már létezik.");
        this.conflictingPerformanceId = Objects.requireNonNull(conflictingPerformanceId);
    }

    public PerformanceId conflictingPerformanceId() {
        return conflictingPerformanceId;
    }
}
