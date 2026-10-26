package hu.gigflow.performance.presentation.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AvailabilityRequest(
        @NotBlank
        @Pattern(
                regexp = "ACCEPTED|TENTATIVE|DECLINED",
                message = "must be ACCEPTED, TENTATIVE or DECLINED")
        String availability) {
}
