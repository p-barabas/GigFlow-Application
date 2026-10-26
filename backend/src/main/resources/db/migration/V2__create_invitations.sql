CREATE TABLE invitations (
    performance_id UUID NOT NULL,
    member_id VARCHAR(100) NOT NULL,
    availability VARCHAR(16),
    responded_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT invitations_pk PRIMARY KEY (performance_id, member_id),
    CONSTRAINT invitations_performance_fk FOREIGN KEY (performance_id)
        REFERENCES performances (performance_id) ON DELETE CASCADE,
    CONSTRAINT invitations_member_fk FOREIGN KEY (member_id)
        REFERENCES members (member_id) ON DELETE RESTRICT,
    CONSTRAINT invitations_availability_chk CHECK (
        availability IS NULL
        OR availability IN ('ACCEPTED', 'TENTATIVE', 'DECLINED')
    ),
    CONSTRAINT invitations_response_state_chk CHECK (
        (availability IS NULL AND responded_at IS NULL)
        OR (availability IS NOT NULL AND responded_at IS NOT NULL)
    ),
    CONSTRAINT invitations_version_non_negative_chk CHECK (version >= 0)
);

CREATE INDEX invitations_member_id_idx
    ON invitations (member_id, performance_id);
