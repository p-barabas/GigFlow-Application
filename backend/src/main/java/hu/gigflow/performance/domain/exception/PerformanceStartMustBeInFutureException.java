package hu.gigflow.performance.domain.exception;

public final class PerformanceStartMustBeInFutureException extends IllegalArgumentException {

    public PerformanceStartMustBeInFutureException() {
        super("A fellépés kezdési időpontjának a jövőben kell lennie.");
    }
}
