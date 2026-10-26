package hu.gigflow.performance.infrastructure.persistence.jpa;

import hu.gigflow.performance.application.port.out.PerformanceRepository;
import hu.gigflow.performance.domain.model.MemberId;
import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceId;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class JpaPerformanceRepositoryAdapter implements PerformanceRepository {

    private final PerformanceSpringDataRepository performanceRepository;
    private final MemberSpringDataRepository memberRepository;
    private final InvitationSpringDataRepository invitationRepository;
    private final EntityManager entityManager;
    private final PerformancePersistenceMapper mapper = new PerformancePersistenceMapper();

    JpaPerformanceRepositoryAdapter(
            PerformanceSpringDataRepository performanceRepository,
            MemberSpringDataRepository memberRepository,
            InvitationSpringDataRepository invitationRepository,
            EntityManager entityManager) {
        this.performanceRepository = performanceRepository;
        this.memberRepository = memberRepository;
        this.invitationRepository = invitationRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Performance> findById(PerformanceId performanceId) {
        return performanceRepository.findById(performanceId.value())
                .map(entity -> mapper.toDomain(
                        entity,
                        invitationRepository.findAllForPerformance(entity.id())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Performance> findAll() {
        List<PerformanceJpaEntity> performanceEntities =
                performanceRepository.findAllByOrderByStartsAtAsc();
        if (performanceEntities.isEmpty()) {
            return List.of();
        }

        List<UUID> performanceIds = performanceEntities.stream()
                .map(PerformanceJpaEntity::id)
                .toList();
        Map<UUID, List<InvitationJpaEntity>> invitationsByPerformance =
                invitationRepository.findAllForPerformances(performanceIds).stream()
                        .collect(Collectors.groupingBy(
                                invitation -> invitation.id().performanceId(),
                                LinkedHashMap::new,
                                Collectors.toList()));

        return performanceEntities.stream()
                .map(entity -> mapper.toDomain(
                        entity,
                        invitationsByPerformance.getOrDefault(entity.id(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public void save(Performance performance) {
        PerformanceSnapshot snapshot = performance.snapshot();

        persistMembers(snapshot.invitedMembers());
        persistPerformance(snapshot);
        persistInvitations(snapshot);
    }

    private void persistMembers(Set<MemberId> memberIds) {
        List<String> ids = memberIds.stream().map(MemberId::value).toList();
        Set<String> existingIds = memberRepository.findAllById(ids).stream()
                .map(MemberJpaEntity::id)
                .collect(Collectors.toSet());
        memberIds.stream()
                .filter(memberId -> !existingIds.contains(memberId.value()))
                .map(mapper::newMemberEntity)
                .forEach(entityManager::persist);
    }

    private void persistPerformance(PerformanceSnapshot snapshot) {
        Optional<PerformanceJpaEntity> existing = performanceRepository.findById(snapshot.id().value());
        if (existing.isPresent()) {
            mapper.updatePerformanceEntity(existing.orElseThrow(), snapshot);
        } else {
            entityManager.persist(mapper.newPerformanceEntity(snapshot));
        }
    }

    private void persistInvitations(PerformanceSnapshot snapshot) {
        Map<String, InvitationJpaEntity> existingByMember =
                invitationRepository.findAllForPerformance(snapshot.id().value()).stream()
                        .collect(Collectors.toMap(
                                invitation -> invitation.id().memberId(),
                                Function.identity()));
        for (MemberId memberId : snapshot.invitedMembers()) {
            InvitationJpaEntity entity = existingByMember.remove(memberId.value());
            if (entity == null) {
                entity = mapper.newInvitationEntity(snapshot, memberId);
                entityManager.persist(entity);
            } else {
                mapper.updateInvitationEntity(
                        entity,
                        snapshot.responses().get(memberId),
                        snapshot.responseTimes().get(memberId));
            }
        }

        existingByMember.values().forEach(entityManager::remove);
    }
}
