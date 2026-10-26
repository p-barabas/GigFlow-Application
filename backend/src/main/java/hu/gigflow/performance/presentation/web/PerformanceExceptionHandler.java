package hu.gigflow.performance.presentation.web;

import hu.gigflow.performance.application.exception.PerformanceConflictException;
import hu.gigflow.performance.application.exception.PerformanceNotFoundException;
import hu.gigflow.performance.domain.exception.EmptyPerformanceTitleException;
import hu.gigflow.performance.domain.exception.EmptyPerformanceVenueException;
import hu.gigflow.performance.domain.exception.MemberNotInvitedException;
import hu.gigflow.performance.domain.exception.PerformanceStartMustBeInFutureException;
import hu.gigflow.performance.domain.exception.ResponseDeadlinePassedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestControllerAdvice
public final class PerformanceExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ProblemDetail> invalidRequest(Exception exception, HttpServletRequest request) {
        List<ValidationViolationResponse> violations = exception instanceof MethodArgumentNotValidException validation
                ? validation.getBindingResult().getFieldErrors().stream()
                        .map(error -> new ValidationViolationResponse(
                                error.getField(),
                                error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage()))
                        .toList()
                : List.of(new ValidationViolationResponse("request", "must contain valid JSON values"));
        return validationProblem(violations, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> invalidDomainInput(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        String field = switch (exception) {
            case EmptyPerformanceTitleException ignored -> "title";
            case EmptyPerformanceVenueException ignored -> "venue";
            case PerformanceStartMustBeInFutureException ignored -> "startsAt";
            default -> "request";
        };
        return validationProblem(
                List.of(new ValidationViolationResponse(field, exception.getMessage())),
                request);
    }

    @ExceptionHandler(DateTimeParseException.class)
    ResponseEntity<ProblemDetail> invalidStartTime(
            DateTimeParseException exception,
            HttpServletRequest request) {
        return validationProblem(
                List.of(new ValidationViolationResponse("startsAt", "must be a valid ISO-8601 UTC timestamp")),
                request);
    }

    @ExceptionHandler(PerformanceNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(
            PerformanceNotFoundException exception,
            HttpServletRequest request) {
        return problem(
                HttpStatus.NOT_FOUND,
                "https://gigflow.example/problems/performance-not-found",
                "Performance not found",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(PerformanceConflictException.class)
    ResponseEntity<ProblemDetail> performanceConflict(
            PerformanceConflictException exception,
            HttpServletRequest request) {
        ProblemDetail problem = createProblem(
                HttpStatus.CONFLICT,
                "https://gigflow.example/problems/performance-conflict",
                "Performance conflict",
                exception.getMessage(),
                request);
        problem.setProperty("conflictingPerformanceId", exception.conflictingPerformanceId().value());
        return response(problem);
    }

    @ExceptionHandler({MemberNotInvitedException.class, ResponseDeadlinePassedException.class})
    ResponseEntity<ProblemDetail> domainConflict(RuntimeException exception, HttpServletRequest request) {
        String type = exception instanceof MemberNotInvitedException
                ? "https://gigflow.example/problems/member-not-invited"
                : "https://gigflow.example/problems/response-deadline-passed";
        return problem(
                HttpStatus.CONFLICT,
                type,
                "Invitation conflict",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpectedError(Exception exception, HttpServletRequest request) {
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "about:blank",
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "The performance list could not be loaded.",
                request);
    }

    private ResponseEntity<ProblemDetail> validationProblem(
            List<ValidationViolationResponse> violations,
            HttpServletRequest request) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "https://gigflow.example/problems/validation-error",
                "Invalid request",
                "The request contains invalid fields.",
                request);
        problem.setProperty("violations", violations);
        return response(problem);
    }

    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest request) {
        return response(createProblem(status, type, title, detail, request));
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    private ResponseEntity<ProblemDetail> response(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
