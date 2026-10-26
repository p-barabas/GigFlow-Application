package hu.gigflow.performance.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface InvitationSpringDataRepository extends JpaRepository<InvitationJpaEntity, InvitationId> {

    @Query("""
            select invitation
            from InvitationJpaEntity invitation
            where invitation.id.performanceId = :performanceId
            order by invitation.id.memberId
            """)
    List<InvitationJpaEntity> findAllForPerformance(@Param("performanceId") UUID performanceId);

    @Query("""
            select invitation
            from InvitationJpaEntity invitation
            where invitation.id.performanceId in :performanceIds
            order by invitation.id.performanceId, invitation.id.memberId
            """)
    List<InvitationJpaEntity> findAllForPerformances(
            @Param("performanceIds") Collection<UUID> performanceIds);
}
