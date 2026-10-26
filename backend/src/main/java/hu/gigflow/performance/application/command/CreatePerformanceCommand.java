package hu.gigflow.performance.application.command;

import hu.gigflow.performance.domain.model.MemberId;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record CreatePerformanceCommand(
        String title,
        String venue,
        Instant startsAt,
        Set<MemberId> invitedMembers) {

    public CreatePerformanceCommand {
        Objects.requireNonNull(invitedMembers, "A meghívottak köre kötelező.");
        invitedMembers = Set.copyOf(invitedMembers);
    }
}
