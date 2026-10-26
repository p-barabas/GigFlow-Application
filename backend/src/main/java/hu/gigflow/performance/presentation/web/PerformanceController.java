package hu.gigflow.performance.presentation.web;

import hu.gigflow.performance.application.command.CreatePerformanceCommand;
import hu.gigflow.performance.application.command.RespondToInvitationCommand;
import hu.gigflow.performance.application.port.in.CreatePerformanceUseCase;
import hu.gigflow.performance.application.port.in.ListPerformancesUseCase;
import hu.gigflow.performance.application.port.in.RespondToInvitationUseCase;
import hu.gigflow.performance.domain.model.Availability;
import hu.gigflow.performance.domain.model.MemberId;
import hu.gigflow.performance.domain.model.PerformanceId;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/performances")
public final class PerformanceController {

    private final ListPerformancesUseCase listPerformances;
    private final CreatePerformanceUseCase createPerformance;
    private final RespondToInvitationUseCase respondToInvitation;
    private final PerformanceResponseMapper responseMapper;

    public PerformanceController(
            ListPerformancesUseCase listPerformances,
            CreatePerformanceUseCase createPerformance,
            RespondToInvitationUseCase respondToInvitation,
            PerformanceResponseMapper responseMapper) {
        this.listPerformances = listPerformances;
        this.createPerformance = createPerformance;
        this.respondToInvitation = respondToInvitation;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    public List<PerformanceResponse> findAll() {
        return listPerformances.findAll().stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<PerformanceResponse> create(
            @Valid @RequestBody CreatePerformanceRequest request) {
        CreatePerformanceCommand command = new CreatePerformanceCommand(
                request.title(),
                request.venue(),
                Instant.parse(request.startsAt()),
                request.invitedMemberIds().stream().map(MemberId::of).collect(Collectors.toSet()));
        PerformanceSnapshot created = createPerformance.create(command);
        URI location = URI.create("/api/performances/" + created.id().value());
        return ResponseEntity.created(location).body(responseMapper.toResponse(created));
    }

    @PutMapping("/{performanceId}/response")
    public PerformanceResponse respond(
            @PathVariable UUID performanceId,
            Authentication authentication,
            @Valid @RequestBody AvailabilityRequest request) {
        PerformanceId id = PerformanceId.of(performanceId);
        return responseMapper.toResponse(respondToInvitation.respond(new RespondToInvitationCommand(
                id,
                MemberId.of(authentication.getName()),
                Availability.valueOf(request.availability()))));
    }
}
