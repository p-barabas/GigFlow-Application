package hu.gigflow.performance.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "invitations")
public class InvitationJpaEntity {

    @EmbeddedId
    private InvitationId id;

    @Column(length = 16)
    private String availability;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected InvitationJpaEntity() {
    }

    InvitationJpaEntity(InvitationId id, String availability, Instant respondedAt) {
        this.id = id;
        this.availability = availability;
        this.respondedAt = respondedAt;
    }

    InvitationId id() {
        return id;
    }

    String availability() {
        return availability;
    }

    Instant respondedAt() {
        return respondedAt;
    }

    void update(String availability, Instant respondedAt) {
        this.availability = availability;
        this.respondedAt = respondedAt;
    }
}
