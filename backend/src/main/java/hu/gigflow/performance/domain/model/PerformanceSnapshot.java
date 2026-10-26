package hu.gigflow.performance.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record PerformanceSnapshot(
        PerformanceId id,
        String title,
        Instant startsAt,
        String venue,
        Set<MemberId> invitedMembers,
        Map<MemberId, Availability> responses,
        Map<MemberId, Instant> responseTimes) {

    public PerformanceSnapshot {
        invitedMembers = Set.copyOf(invitedMembers);
        responses = Map.copyOf(responses);
        responseTimes = Map.copyOf(responseTimes);
        if (!responses.keySet().equals(responseTimes.keySet())) {
            throw new IllegalArgumentException("Minden válaszhoz pontosan egy válaszidő tartozik.");
        }
    }

}
