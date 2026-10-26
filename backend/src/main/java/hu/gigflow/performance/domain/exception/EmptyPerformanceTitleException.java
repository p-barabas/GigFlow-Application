package hu.gigflow.performance.domain.exception;

public final class EmptyPerformanceTitleException extends IllegalArgumentException {

    public EmptyPerformanceTitleException() {
        super("A fellépés címe nem lehet üres.");
    }
}
