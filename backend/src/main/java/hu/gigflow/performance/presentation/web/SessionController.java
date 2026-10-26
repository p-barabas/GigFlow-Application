package hu.gigflow.performance.presentation.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/session")
public final class SessionController {

    @GetMapping
    public UserSessionResponse current(Authentication authentication) {
        String username = authentication.getName();
        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .collect(Collectors.toUnmodifiableSet());
        return new UserSessionResponse(
                username,
                displayName(username),
                roles.contains("MEMBER") ? username : null,
                roles);
    }

    private String displayName(String username) {
        return switch (username) {
            case "anna" -> "Anna";
            case "bence" -> "Bence";
            case "csilla" -> "Csilla";
            case "leader" -> "Zenekarvezető";
            default -> username;
        };
    }
}
