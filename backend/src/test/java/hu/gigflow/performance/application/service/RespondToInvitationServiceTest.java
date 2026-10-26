package hu.gigflow.performance.application.service;

import hu.gigflow.performance.application.command.RespondToInvitationCommand;
import hu.gigflow.performance.application.exception.PerformanceNotFoundException;
import hu.gigflow.performance.application.port.out.PerformanceRepository;
import hu.gigflow.performance.domain.exception.ResponseDeadlinePassedException;
import hu.gigflow.performance.domain.model.Availability;
import hu.gigflow.performance.domain.model.MemberId;
import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceId;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RespondToInvitationServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-06T12:00:00Z");
    private static final Instant STARTS_AT = Instant.parse("2026-10-06T18:00:00Z");
    private static final PerformanceId PERFORMANCE_ID =
            PerformanceId.of(UUID.fromString("5d9961bb-001a-4ca0-9125-e36cec98d755"));
    private static final MemberId ANNA = MemberId.of("anna");

    @Test
    void invitedMemberCanRespondBeforeStartAndPerformanceIsSaved() {
        InMemoryTestRepository repository = repositoryWithPerformance();
        RespondToInvitationService service = service(repository, NOW);

        PerformanceSnapshot result = service.respond(command(Availability.ACCEPTED));

        assertEquals(Availability.ACCEPTED, repository.performance.snapshot().responses().get(ANNA));
        assertEquals(Availability.ACCEPTED, result.responses().get(ANNA));
        assertEquals(1, repository.saveCount);
    }

    @Test
    void newResponseOverwritesPreviousResponseForSameMember() {
        InMemoryTestRepository repository = repositoryWithPerformance();
        repository.performance.respond(ANNA, Availability.ACCEPTED, NOW.minusSeconds(60));
        RespondToInvitationService service = service(repository, NOW);

        PerformanceSnapshot result = service.respond(command(Availability.TENTATIVE));

        assertEquals(1, repository.performance.snapshot().responses().size());
        assertEquals(Availability.TENTATIVE, repository.performance.snapshot().responses().get(ANNA));
        assertEquals(Availability.TENTATIVE, result.responses().get(ANNA));
        assertEquals(1, repository.saveCount);
    }

    @Test
    void responseAtDeadlineIsRejectedAndPerformanceIsNotSaved() {
        InMemoryTestRepository repository = repositoryWithPerformance();
        RespondToInvitationService service = service(repository, STARTS_AT);

        assertThrows(ResponseDeadlinePassedException.class, () -> service.respond(command(Availability.ACCEPTED)));
        assertEquals(0, repository.saveCount);
        assertEquals(0, repository.performance.snapshot().responses().size());
    }

    @Test
    void missingPerformanceProducesTargetedNotFoundError() {
        InMemoryTestRepository repository = new InMemoryTestRepository(null);
        RespondToInvitationService service = service(repository, NOW);

        PerformanceNotFoundException exception = assertThrows(
                PerformanceNotFoundException.class,
                () -> service.respond(command(Availability.ACCEPTED)));

        assertEquals(PERFORMANCE_ID, exception.performanceId());
        assertEquals(0, repository.saveCount);
    }

    private static RespondToInvitationCommand command(Availability availability) {
        return new RespondToInvitationCommand(PERFORMANCE_ID, ANNA, availability);
    }

    private static RespondToInvitationService service(InMemoryTestRepository repository, Instant now) {
        return new RespondToInvitationService(repository, Clock.fixed(now, ZoneOffset.UTC));
    }

    private static InMemoryTestRepository repositoryWithPerformance() {
        Performance performance = Performance.schedule(
                PERFORMANCE_ID,
                "Őszi koncert",
                STARTS_AT,
                "Bartók Terem",
                Set.of(ANNA),
                NOW.minusSeconds(3600));
        return new InMemoryTestRepository(performance);
    }

    private static final class InMemoryTestRepository implements PerformanceRepository {
        private Performance performance;
        private int saveCount;

        private InMemoryTestRepository(Performance performance) {
            this.performance = performance;
        }

        @Override
        public Optional<Performance> findById(PerformanceId performanceId) {
            if (performance == null || !performance.snapshot().id().equals(performanceId)) {
                return Optional.empty();
            }
            return Optional.of(performance);
        }

        @Override
        public List<Performance> findAll() {
            return performance == null ? List.of() : List.of(performance);
        }

        @Override
        public void save(Performance performance) {
            this.performance = performance;
            saveCount++;
        }
    }
}
