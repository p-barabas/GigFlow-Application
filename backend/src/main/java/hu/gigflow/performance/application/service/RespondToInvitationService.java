package hu.gigflow.performance.application.service;

import hu.gigflow.performance.application.command.RespondToInvitationCommand;
import hu.gigflow.performance.application.exception.PerformanceNotFoundException;
import hu.gigflow.performance.application.port.in.RespondToInvitationUseCase;
import hu.gigflow.performance.application.port.out.PerformanceRepository;
import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;

import java.time.Clock;
import java.util.Objects;

public final class RespondToInvitationService implements RespondToInvitationUseCase {

    private final PerformanceRepository repository;
    private final Clock clock;

    public RespondToInvitationService(PerformanceRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public PerformanceSnapshot respond(RespondToInvitationCommand command) {
        Objects.requireNonNull(command, "A válaszadási parancs kötelező.");
        Performance performance = repository.findById(command.performanceId())
                .orElseThrow(() -> new PerformanceNotFoundException(command.performanceId()));

        performance.respond(command.memberId(), command.availability(), clock.instant());
        repository.save(performance);
        return performance.snapshot();
    }
}
