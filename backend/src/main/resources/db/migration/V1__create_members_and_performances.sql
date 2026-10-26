CREATE TABLE members (
    member_id VARCHAR(100) NOT NULL,
    CONSTRAINT members_pk PRIMARY KEY (member_id),
    CONSTRAINT members_member_id_trimmed_chk CHECK (member_id = btrim(member_id)),
    CONSTRAINT members_member_id_not_blank_chk CHECK (length(member_id) > 0)
);

CREATE TABLE performances (
    performance_id UUID NOT NULL,
    title TEXT NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    venue TEXT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT performances_pk PRIMARY KEY (performance_id),
    CONSTRAINT performances_title_not_blank_chk CHECK (length(btrim(title)) > 0),
    CONSTRAINT performances_venue_not_blank_chk CHECK (length(btrim(venue)) > 0),
    CONSTRAINT performances_version_non_negative_chk CHECK (version >= 0)
);

CREATE INDEX performances_starts_at_idx
    ON performances (starts_at, performance_id);
