package hu.gigflow.performance.infrastructure.persistence;

import hu.gigflow.performance.domain.model.Availability;
import hu.gigflow.performance.domain.model.MemberId;
import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceId;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SamplePerformanceData {

    public static final PerformanceId AUTUMN_CONCERT_ID =
            PerformanceId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    public static final PerformanceId CLUB_NIGHT_ID =
            PerformanceId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));

    private static final MemberId ANNA = MemberId.of("anna");
    private static final MemberId BENCE = MemberId.of("bence");
    private static final MemberId CSILLA = MemberId.of("csilla");
    private static final Set<MemberId> BAND_MEMBERS = Set.of(ANNA, BENCE, CSILLA);

    private SamplePerformanceData() {
    }

    public static void loadInto(InMemoryPerformanceRepository repository) {
        repository.save(Performance.restore(new PerformanceSnapshot(
                AUTUMN_CONCERT_ID,
                "Őszi koncert",
                Instant.parse("2099-10-17T18:00:00Z"),
                "Városi Művelődési Ház",
                BAND_MEMBERS,
                Map.of(ANNA, Availability.ACCEPTED, BENCE, Availability.TENTATIVE),
                Map.of(
                        ANNA, Instant.parse("2099-10-01T12:00:00Z"),
                        BENCE, Instant.parse("2099-10-02T12:00:00Z")))));
        repository.save(Performance.restore(new PerformanceSnapshot(
                CLUB_NIGHT_ID,
                "Klubest a Rakparton",
                Instant.parse("2099-11-06T20:30:00Z"),
                "Rakpart Klub",
                BAND_MEMBERS,
                Map.of(ANNA, Availability.TENTATIVE, CSILLA, Availability.DECLINED),
                Map.of(
                        ANNA, Instant.parse("2099-11-01T12:00:00Z"),
                        CSILLA, Instant.parse("2099-11-02T12:00:00Z")))));
    }
}
