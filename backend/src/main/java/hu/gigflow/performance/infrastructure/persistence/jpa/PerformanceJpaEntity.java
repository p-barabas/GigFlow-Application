package hu.gigflow.performance.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "performances")
public class PerformanceJpaEntity {

    @Id
    @Column(name = "performance_id", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(nullable = false)
    private String venue;

    @Version
    @Column(nullable = false)
    private long version;

    protected PerformanceJpaEntity() {
    }

    PerformanceJpaEntity(UUID id, String title, Instant startsAt, String venue) {
        this.id = id;
        this.title = title;
        this.startsAt = startsAt;
        this.venue = venue;
    }

    UUID id() {
        return id;
    }

    String title() {
        return title;
    }

    Instant startsAt() {
        return startsAt;
    }

    String venue() {
        return venue;
    }

    void update(String title, Instant startsAt, String venue) {
        this.title = title;
        this.startsAt = startsAt;
        this.venue = venue;
    }
}
