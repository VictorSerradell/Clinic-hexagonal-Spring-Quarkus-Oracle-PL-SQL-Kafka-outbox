-- init scripts run as SYS in the CDB root: switch to the pluggable DB first
ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = clinic;

-- Transactional Outbox: events written in the same transaction as the business change
CREATE TABLE outbox_events (
    id            VARCHAR2(36)   PRIMARY KEY,
    aggregate_id  VARCHAR2(36)   NOT NULL,
    event_type    VARCHAR2(100)  NOT NULL,
    payload       VARCHAR2(4000) NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at  TIMESTAMP WITH TIME ZONE
);

-- Pending rows (published_at IS NULL) are found through this composite index
CREATE INDEX ix_outbox_pending ON outbox_events (published_at, created_at);
