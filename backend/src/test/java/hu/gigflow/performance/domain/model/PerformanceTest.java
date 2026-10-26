package hu.gigflow.performance.domain.model;

import hu.gigflow.performance.domain.exception.MemberNotInvitedException;
import hu.gigflow.performance.domain.exception.ResponseDeadlinePassedException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PerformanceTest {

    private static final Instant NOW = Instant.parse("2026-10-06T12:00:00Z");
    private static final Instant STARTS_AT = Instant.parse("2026-10-06T18:00:00Z");
    private static final MemberId ANNA = MemberId.of("anna");

    @Test
    void invitedMemberCanOverwritePreviousResponseBeforeStart() {
        Performance performance = performanceWithInvite(ANNA);

        performance.respond(ANNA, Availability.ACCEPTED, NOW);
        performance.respond(ANNA, Availability.TENTATIVE, NOW.plusSeconds(60));

        assertEquals(1, performance.snapshot().responses().size());
        assertEquals(Availability.TENTATIVE, performance.snapshot().responses().get(ANNA));
        assertEquals(NOW.plusSeconds(60), performance.snapshot().responseTimes().get(ANNA));
    }

    @Test
    void repeatedAvailabilityStillRecordsTheNewResponseTime() {
        Performance performance = performanceWithInvite(ANNA);

        performance.respond(ANNA, Availability.ACCEPTED, NOW);
        performance.respond(ANNA, Availability.ACCEPTED, NOW.plusSeconds(60));

        assertEquals(Availability.ACCEPTED, performance.snapshot().responses().get(ANNA));
        assertEquals(NOW.plusSeconds(60), performance.snapshot().responseTimes().get(ANNA));
    }

    @Test
    void memberWhoWasNotInvitedCannotRespond() {
        Performance performance = performanceWithInvite(ANNA);

        assertThrows(
                MemberNotInvitedException.class,
                () -> performance.respond(MemberId.of("bence"), Availability.ACCEPTED, NOW));
        assertEquals(0, performance.snapshot().responses().size());
    }

    @Test
    void responseAtPerformanceStartIsRejected() {
        Performance performance = performanceWithInvite(ANNA);

        assertThrows(
                ResponseDeadlinePassedException.class,
                () -> performance.respond(ANNA, Availability.ACCEPTED, STARTS_AT));
        assertEquals(0, performance.snapshot().responses().size());
    }

    private static Performance performanceWithInvite(MemberId memberId) {
        return Performance.schedule(
                PerformanceId.of(UUID.fromString("0edfb17c-72b1-47ca-83cb-30910d7e9a71")),
                "Őszi koncert",
                STARTS_AT,
                "Bartók Terem",
                Set.of(memberId),
                NOW);
    }
}
