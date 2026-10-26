package hu.gigflow.performance.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface PerformanceSpringDataRepository extends JpaRepository<PerformanceJpaEntity, UUID> {

    List<PerformanceJpaEntity> findAllByOrderByStartsAtAsc();
}
