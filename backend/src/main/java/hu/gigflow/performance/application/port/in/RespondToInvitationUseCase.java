package hu.gigflow.performance.application.port.in;

import hu.gigflow.performance.application.command.RespondToInvitationCommand;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;

public interface RespondToInvitationUseCase {

    PerformanceSnapshot respond(RespondToInvitationCommand command);
}
