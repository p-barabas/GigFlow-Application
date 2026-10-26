package hu.gigflow.performance.application.service;

import hu.gigflow.performance.application.command.CreatePerformanceCommand;
import hu.gigflow.performance.application.exception.PerformanceConflictException;
import hu.gigflow.performance.application.port.out.PerformanceIdGenerator;
import hu.gigflow.performance.application.port.out.PerformanceRepository;
import hu.gigflow.performance.domain.exception.EmptyPerformanceTitleException;
import hu.gigflow.performance.domain.exception.EmptyPerformanceVenueException;
import hu.gigflow.performance.domain.exception.PerformanceStartMustBeInFutureException;
import hu.gigflow.performance.domain.model.MemberId;
import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceId;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreatePerformanceServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-06T12:00:00Z");
    private static final PerformanceId GENERATED_ID =
            PerformanceId.of(UUID.fromString("d7118428-13d2-45ce-8065-107577527750"));

    private CapturingPerformanceRepository repository;
    private CreatePerformanceService service;

    @BeforeEach
    void setUp() {
        repository = new CapturingPerformanceRepository();
        PerformanceIdGenerator idGenerator = () -> GENERATED_ID;
        Clock utcClock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new CreatePerformanceService(repository, idGenerator, utcClock);
    }

    @Test
    void createsAndSavesPerformanceThenReturnsGeneratedId() {
        Instant startsAt = Instant.parse("2026-10-07T18:00:00Z");
        Set<MemberId> invitedMembers = Set.of(MemberId.of("anna"), MemberId.of("bence"));

        PerformanceSnapshot result = service.create(
                new CreatePerformanceCommand("  Őszi koncert  ", "  Bartók Terem  ", startsAt, invitedMembers));

        assertEquals(GENERATED_ID, result.id());
        assertEquals("Őszi koncert", result.title());
        assertEquals("Bartók Terem", result.venue());
        assertEquals(startsAt, result.startsAt());
        assertEquals(invitedMembers, result.invitedMembers());
        assertNotNull(repository.saved);
        assertEquals(GENERATED_ID, repository.saved.snapshot().id());
        assertEquals("Őszi koncert", repository.saved.snapshot().title());
        assertEquals("Bartók Terem", repository.saved.snapshot().venue());
        assertEquals(startsAt, repository.saved.snapshot().startsAt());
        assertEquals(invitedMembers, repository.saved.snapshot().invitedMembers());
    }

    @Test
    void rejectsEmptyTitleWithoutSaving() {
        assertThrows(EmptyPerformanceTitleException.class, () -> service.create(validCommand("   ", "Bartók Terem", futureStart())));
        assertEquals(0, repository.saveCount);
    }

    @Test
    void rejectsEmptyVenueWithoutSaving() {
        assertThrows(EmptyPerformanceVenueException.class, () -> service.create(validCommand("Őszi koncert", "   ", futureStart())));
        assertEquals(0, repository.saveCount);
    }

    @Test
    void rejectsStartThatIsNotInTheFutureWithoutSaving() {
        assertThrows(PerformanceStartMustBeInFutureException.class, () -> service.create(validCommand("Őszi koncert", "Bartók Terem", NOW)));
        assertEquals(0, repository.saveCount);
    }

    @Test
    void rejectsDuplicatePerformanceAndReturnsConflictingId() {
        CreatePerformanceCommand command = validCommand("Őszi koncert", "Bartók Terem", futureStart());
        service.create(command);

        PerformanceConflictException exception = assertThrows(
                PerformanceConflictException.class,
                () -> service.create(command));

        assertEquals(GENERATED_ID, exception.conflictingPerformanceId());
        assertEquals(1, repository.saveCount);
    }

    private static CreatePerformanceCommand validCommand(String title, String venue, Instant startsAt) {
        return new CreatePerformanceCommand(title, venue, startsAt, Set.of(MemberId.of("anna")));
    }

    private static Instant futureStart() {
        return NOW.plusSeconds(3600);
    }

    private static final class CapturingPerformanceRepository implements PerformanceRepository {
        private Performance saved;
        private int saveCount;

        @Override
        public Optional<Performance> findById(PerformanceId performanceId) {
            if (saved == null || !saved.snapshot().id().equals(performanceId)) {
                return Optional.empty();
            }
            return Optional.of(saved);
        }

        @Override
        public List<Performance> findAll() {
            return saved == null ? List.of() : List.of(saved);
        }

        @Override
        public void save(Performance performance) {
            saved = performance;
            saveCount++;
        }
    }
}
