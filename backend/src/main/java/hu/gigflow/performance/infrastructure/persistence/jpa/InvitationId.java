package hu.gigflow.performance.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class InvitationId implements Serializable {

    @Column(name = "performance_id", nullable = false)
    private UUID performanceId;

    @Column(name = "member_id", nullable = false, length = 100)
    private String memberId;

    protected InvitationId() {
    }

    InvitationId(UUID performanceId, String memberId) {
        this.performanceId = Objects.requireNonNull(performanceId);
        this.memberId = Objects.requireNonNull(memberId);
    }

    UUID performanceId() {
        return performanceId;
    }

    String memberId() {
        return memberId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof InvitationId that)) {
            return false;
        }
        return performanceId.equals(that.performanceId) && memberId.equals(that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(performanceId, memberId);
    }
}
