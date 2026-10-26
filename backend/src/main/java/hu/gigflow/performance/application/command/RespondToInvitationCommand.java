package hu.gigflow.performance.application.command;

import hu.gigflow.performance.domain.model.Availability;
import hu.gigflow.performance.domain.model.MemberId;
import hu.gigflow.performance.domain.model.PerformanceId;

import java.util.Objects;

public record RespondToInvitationCommand(
        PerformanceId performanceId,
        MemberId memberId,
        Availability availability) {

    public RespondToInvitationCommand {
        Objects.requireNonNull(performanceId, "A fellépés azonosítója kötelező.");
        Objects.requireNonNull(memberId, "A tag azonosítója kötelező.");
        Objects.requireNonNull(availability, "A válasz kötelező.");
    }
}
