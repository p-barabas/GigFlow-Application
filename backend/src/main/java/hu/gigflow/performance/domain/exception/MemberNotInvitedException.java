package hu.gigflow.performance.domain.exception;

public final class MemberNotInvitedException extends IllegalStateException {

    public MemberNotInvitedException() {
        super("Csak meghívott zenekari tag válaszolhat a fellépésre.");
    }
}
