package hu.gigflow.performance.presentation.web;

import java.util.Set;

public record UserSessionResponse(
        String username,
        String displayName,
        String memberId,
        Set<String> roles) {
}
