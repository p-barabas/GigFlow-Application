package hu.gigflow.performance.domain.exception;

public final class ResponseDeadlinePassedException extends IllegalStateException {

    public ResponseDeadlinePassedException() {
        super("A fellépés kezdetekor vagy azt követően már nem adható válasz.");
    }
}
