package hu.gigflow.performance.presentation.web;

import hu.gigflow.performance.domain.model.PerformanceSnapshot;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
final class PerformanceResponseMapper {

    PerformanceResponse toResponse(PerformanceSnapshot snapshot) {
        Map<String, String> responses = snapshot.responses().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        entry -> entry.getKey().value(),
                        entry -> entry.getValue().name()));
        var invitedMemberIds = snapshot.invitedMembers().stream()
                .map(memberId -> memberId.value())
                .collect(Collectors.toUnmodifiableSet());

        return new PerformanceResponse(
                snapshot.id().value(),
                snapshot.title(),
                snapshot.startsAt(),
                snapshot.venue(),
                invitedMemberIds,
                responses);
    }
}
