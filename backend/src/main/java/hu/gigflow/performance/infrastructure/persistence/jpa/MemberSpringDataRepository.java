package hu.gigflow.performance.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

interface MemberSpringDataRepository extends JpaRepository<MemberJpaEntity, String> {
}
