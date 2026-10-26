package hu.gigflow.performance.infrastructure.persistence.jpa;

import hu.gigflow.performance.domain.model.Availability;
import hu.gigflow.performance.domain.model.MemberId;
import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceId;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

final class PerformancePersistenceMapper {

    Performance toDomain(
            PerformanceJpaEntity performanceEntity,
            List<InvitationJpaEntity> invitationEntities) {
        LinkedHashSet<MemberId> invitedMembers = new LinkedHashSet<>();
        Map<MemberId, Availability> responses = new LinkedHashMap<>();
        Map<MemberId, Instant> responseTimes = new LinkedHashMap<>();

        for (InvitationJpaEntity invitationEntity : invitationEntities) {
            MemberId memberId = MemberId.of(invitationEntity.id().memberId());
            invitedMembers.add(memberId);
            if (invitationEntity.availability() != null) {
                responses.put(memberId, Availability.valueOf(invitationEntity.availability()));
                responseTimes.put(memberId, invitationEntity.respondedAt());
            }
        }

        return Performance.restore(new PerformanceSnapshot(
                PerformanceId.of(performanceEntity.id()),
                performanceEntity.title(),
                performanceEntity.startsAt(),
                performanceEntity.venue(),
                invitedMembers,
                responses,
                responseTimes));
    }

    PerformanceJpaEntity newPerformanceEntity(PerformanceSnapshot snapshot) {
        return new PerformanceJpaEntity(
                snapshot.id().value(),
                snapshot.title(),
                snapshot.startsAt(),
                snapshot.venue());
    }

    void updatePerformanceEntity(
            PerformanceJpaEntity entity,
            PerformanceSnapshot snapshot) {
        entity.update(snapshot.title(), snapshot.startsAt(), snapshot.venue());
    }

    MemberJpaEntity newMemberEntity(MemberId memberId) {
        return new MemberJpaEntity(memberId.value());
    }

    InvitationJpaEntity newInvitationEntity(
            PerformanceSnapshot snapshot,
            MemberId memberId) {
        Availability availability = snapshot.responses().get(memberId);
        return new InvitationJpaEntity(
                new InvitationId(snapshot.id().value(), memberId.value()),
                availabilityName(availability),
                snapshot.responseTimes().get(memberId));
    }

    void updateInvitationEntity(
            InvitationJpaEntity entity,
            Availability availability,
            Instant respondedAt) {
        entity.update(availabilityName(availability), respondedAt);
    }

    private String availabilityName(Availability availability) {
        return availability == null ? null : availability.name();
    }
}
