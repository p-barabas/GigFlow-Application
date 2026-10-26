package hu.gigflow.performance.presentation.web;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record PerformanceResponse(
        UUID id,
        String title,
        Instant startsAt,
        String venue,
        Set<String> invitedMemberIds,
        Map<String, String> responses) {
}
