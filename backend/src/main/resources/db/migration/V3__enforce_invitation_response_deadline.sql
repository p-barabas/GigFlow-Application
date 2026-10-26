CREATE FUNCTION check_invitation_response_deadline()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    performance_start TIMESTAMPTZ;
BEGIN
    IF NEW.responded_at IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT starts_at
      INTO performance_start
      FROM performances
     WHERE performance_id = NEW.performance_id
       FOR SHARE;

    IF performance_start IS NOT NULL AND NEW.responded_at >= performance_start THEN
        RAISE EXCEPTION 'Invitation response must precede the performance start.'
            USING ERRCODE = '23514',
                  CONSTRAINT = 'invitations_responded_before_start_chk';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER invitations_response_deadline_trg
BEFORE INSERT OR UPDATE OF performance_id, responded_at
ON invitations
FOR EACH ROW
EXECUTE FUNCTION check_invitation_response_deadline();

CREATE FUNCTION check_performance_start_against_responses()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM invitations
         WHERE performance_id = NEW.performance_id
           AND responded_at IS NOT NULL
           AND responded_at >= NEW.starts_at
    ) THEN
        RAISE EXCEPTION 'Performance start must follow every recorded invitation response.'
            USING ERRCODE = '23514',
                  CONSTRAINT = 'performances_start_after_responses_chk';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER performances_start_after_responses_trg
BEFORE UPDATE OF starts_at
ON performances
FOR EACH ROW
EXECUTE FUNCTION check_performance_start_against_responses();
