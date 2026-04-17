CREATE TABLE user_providers (
    id            BIGINT AUTO_INCREMENT NOT NULL,
    member_id     BIGINT                NOT NULL,
    provider_type VARCHAR(20)           NOT NULL,
    provider_id   VARCHAR(255)          NOT NULL,
    created_at    DATETIME(6)           NOT NULL,
    CONSTRAINT pk_user_providers PRIMARY KEY (id),
    CONSTRAINT uq_provider_type_id UNIQUE (provider_type, provider_id),
    CONSTRAINT fk_user_providers_member FOREIGN KEY (member_id) REFERENCES members (id)
);
