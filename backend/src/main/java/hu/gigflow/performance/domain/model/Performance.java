package hu.gigflow.performance.domain.model;

import hu.gigflow.performance.domain.exception.EmptyPerformanceTitleException;
import hu.gigflow.performance.domain.exception.EmptyPerformanceVenueException;
import hu.gigflow.performance.domain.exception.MemberNotInvitedException;
import hu.gigflow.performance.domain.exception.PerformanceStartMustBeInFutureException;
import hu.gigflow.performance.domain.exception.ResponseDeadlinePassedException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Performance {

    private final PerformanceId id;
    private final String title;
    private final Instant startsAt;
    private final String venue;
    private final Set<MemberId> invitedMembers;
    private final Map<MemberId, Availability> responses;
    private final Map<MemberId, Instant> responseTimes;

    private Performance(
            PerformanceId id,
            String title,
            Instant startsAt,
            String venue,
            Set<MemberId> invitedMembers,
            Map<MemberId, Availability> responses,
            Map<MemberId, Instant> responseTimes) {
        this.id = Objects.requireNonNull(id, "A fellépés azonosítója kötelező.");
        this.title = requireTitle(title);
        this.startsAt = Objects.requireNonNull(startsAt, "A kezdési időpont kötelező.");
        this.venue = requireVenue(venue);
        this.invitedMembers = Set.copyOf(Objects.requireNonNull(invitedMembers, "A meghívottak köre kötelező."));
        this.responses = new LinkedHashMap<>(Objects.requireNonNull(responses, "A válaszok kötelezők."));
        this.responseTimes = new LinkedHashMap<>(Objects.requireNonNull(responseTimes, "A válaszidők kötelezők."));
        if (!this.invitedMembers.containsAll(this.responses.keySet())) {
            throw new MemberNotInvitedException();
        }
        if (!this.responses.keySet().equals(this.responseTimes.keySet())) {
            throw new IllegalArgumentException("Minden válaszhoz pontosan egy válaszidő tartozik.");
        }
    }

    public static Performance schedule(
            PerformanceId id,
            String title,
            Instant startsAt,
            String venue,
            Set<MemberId> invitedMembers,
            Instant now) {
        Objects.requireNonNull(now, "Az aktuális idő kötelező.");
        if (startsAt == null || !startsAt.isAfter(now)) {
            throw new PerformanceStartMustBeInFutureException();
        }
        return new Performance(id, title, startsAt, venue, invitedMembers, Map.of(), Map.of());
    }

    public static Performance restore(PerformanceSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "A visszaállítandó állapot kötelező.");
        return new Performance(
                snapshot.id(),
                snapshot.title(),
                snapshot.startsAt(),
                snapshot.venue(),
                snapshot.invitedMembers(),
                snapshot.responses(),
                snapshot.responseTimes());
    }

    public void respond(MemberId memberId, Availability availability, Instant now) {
        Objects.requireNonNull(memberId, "A tag azonosítója kötelező.");
        Objects.requireNonNull(availability, "A válasz kötelező.");
        Objects.requireNonNull(now, "Az aktuális idő kötelező.");

        if (!invitedMembers.contains(memberId)) {
            throw new MemberNotInvitedException();
        }
        if (!now.isBefore(startsAt)) {
            throw new ResponseDeadlinePassedException();
        }
        responses.put(memberId, availability);
        responseTimes.put(memberId, now);
    }

    public PerformanceSnapshot snapshot() {
        return new PerformanceSnapshot(id, title, startsAt, venue, invitedMembers, responses, responseTimes);
    }

    private static String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new EmptyPerformanceTitleException();
        }
        return title.trim();
    }

    private static String requireVenue(String venue) {
        if (venue == null || venue.isBlank()) {
            throw new EmptyPerformanceVenueException();
        }
        return venue.trim();
    }
}
