package hu.gigflow.performance.presentation.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreatePerformanceRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 200) String venue,
        @NotBlank
        @Pattern(regexp = ".*Z$", message = "must be an ISO-8601 UTC timestamp ending in Z")
        String startsAt,
        @NotEmpty Set<@NotBlank String> invitedMemberIds) {
}
