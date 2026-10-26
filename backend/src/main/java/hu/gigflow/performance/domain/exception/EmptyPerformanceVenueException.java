package hu.gigflow.performance.domain.exception;

public final class EmptyPerformanceVenueException extends IllegalArgumentException {

    public EmptyPerformanceVenueException() {
        super("A fellépés helyszíne nem lehet üres.");
    }
}
