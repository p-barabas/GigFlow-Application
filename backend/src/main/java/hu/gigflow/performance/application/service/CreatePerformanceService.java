package hu.gigflow.performance.application.service;

import hu.gigflow.performance.application.command.CreatePerformanceCommand;
import hu.gigflow.performance.application.exception.PerformanceConflictException;
import hu.gigflow.performance.application.port.in.CreatePerformanceUseCase;
import hu.gigflow.performance.application.port.out.PerformanceIdGenerator;
import hu.gigflow.performance.application.port.out.PerformanceRepository;
import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceId;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;

import java.time.Clock;
import java.util.Objects;

public final class CreatePerformanceService implements CreatePerformanceUseCase {

    private final PerformanceRepository repository;
    private final PerformanceIdGenerator idGenerator;
    private final Clock clock;

    public CreatePerformanceService(
            PerformanceRepository repository,
            PerformanceIdGenerator idGenerator,
            Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public PerformanceSnapshot create(CreatePerformanceCommand command) {
        Objects.requireNonNull(command, "A létrehozási parancs kötelező.");
        PerformanceId performanceId = idGenerator.nextId();
        Performance performance = Performance.schedule(
                performanceId,
                command.title(),
                command.startsAt(),
                command.venue(),
                command.invitedMembers(),
                clock.instant());

        PerformanceSnapshot candidate = performance.snapshot();
        repository.findAll().stream()
                .map(Performance::snapshot)
                .filter(existing -> existing.title().equals(candidate.title()))
                .filter(existing -> existing.venue().equals(candidate.venue()))
                .filter(existing -> existing.startsAt().equals(candidate.startsAt()))
                .findFirst()
                .ifPresent(existing -> {
                    throw new PerformanceConflictException(existing.id());
                });

        repository.save(performance);
        return performance.snapshot();
    }
}
