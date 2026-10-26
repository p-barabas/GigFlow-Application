package hu.gigflow.performance.infrastructure.persistence.jpa;

import hu.gigflow.performance.application.port.out.PerformanceRepository;
import hu.gigflow.performance.domain.model.Availability;
import hu.gigflow.performance.domain.model.MemberId;
import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JpaPerformanceRepositoryAdapterTest {

    private static final String MEMBER_ID = "adapter-test-anna";
    private static final Instant FIRST_RESPONSE = Instant.parse("2099-12-01T12:00:00Z");
    private static final Instant SECOND_RESPONSE = Instant.parse("2099-12-02T12:00:00Z");

    @Autowired
    private PerformanceRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID performanceUuid;

    @Test
    void savesRestoresAndOverwritesAResponse() {
        performanceUuid = UUID.randomUUID();
        PerformanceId performanceId = PerformanceId.of(performanceUuid);
        MemberId anna = MemberId.of(MEMBER_ID);
        Instant start = Instant.parse("2099-12-20T19:00:00Z");
        Performance performance = Performance.schedule(
                performanceId,
                "Adapter tesztkoncert",
                start,
                "Tesztterem",
                Set.of(anna),
                Instant.parse("2099-01-01T00:00:00Z"));

        repository.save(performance);
        flushAndClear();
        Performance restored = repository.findById(performanceId).orElseThrow();
        restored.respond(anna, Availability.ACCEPTED, FIRST_RESPONSE);
        repository.save(restored);
        flushAndClear();

        Performance reloaded = repository.findById(performanceId).orElseThrow();
        assertThat(reloaded.snapshot().responses())
                .containsEntry(anna, Availability.ACCEPTED);
        assertThat(reloaded.snapshot().responseTimes())
                .containsEntry(anna, FIRST_RESPONSE);

        reloaded.respond(anna, Availability.DECLINED, SECOND_RESPONSE);
        repository.save(reloaded);
        flushAndClear();

        var finalSnapshot = repository.findById(performanceId).orElseThrow().snapshot();
        assertThat(finalSnapshot.responses())
                .containsEntry(anna, Availability.DECLINED);
        assertThat(finalSnapshot.responseTimes())
                .containsEntry(anna, SECOND_RESPONSE);
        assertThat(persistedResponseTime()).isEqualTo(SECOND_RESPONSE);
        assertThat(databaseProduct()).isEqualTo("PostgreSQL");
    }

    @AfterTransaction
    void changesAreRolledBack() {
        assertThat(count("performances", "performance_id", performanceUuid)).isZero();
        assertThat(count("members", "member_id", MEMBER_ID)).isZero();
        assertThat(count("invitations", "performance_id", performanceUuid)).isZero();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Instant persistedResponseTime() {
        OffsetDateTime value = jdbcTemplate.queryForObject(
                "SELECT responded_at FROM invitations WHERE performance_id = ? AND member_id = ?",
                OffsetDateTime.class,
                performanceUuid,
                MEMBER_ID);
        return value.toInstant();
    }

    private String databaseProduct() {
        return jdbcTemplate.execute((ConnectionCallback<String>) connection ->
                connection.getMetaData().getDatabaseProductName());
    }

    private long count(String table, String column, Object value) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM " + table + " WHERE " + column + " = ?",
                Long.class,
                value);
        return count;
    }
}
