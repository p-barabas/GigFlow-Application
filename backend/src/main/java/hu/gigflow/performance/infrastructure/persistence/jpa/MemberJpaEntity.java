package hu.gigflow.performance.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class MemberJpaEntity {

    @Id
    @Column(name = "member_id", nullable = false, updatable = false, length = 100)
    private String id;

    protected MemberJpaEntity() {
    }

    MemberJpaEntity(String id) {
        this.id = id;
    }

    String id() {
        return id;
    }
}
