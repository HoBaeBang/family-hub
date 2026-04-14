CREATE TABLE members (
     id         BIGINT AUTO_INCREMENT NOT NULL,
     email      VARCHAR(100)          NOT NULL UNIQUE,
     password   VARCHAR(255)          NOT NULL,
     name       VARCHAR(50)           NOT NULL,
     role       VARCHAR(20)           NOT NULL,
     created_at DATETIME(6)           NOT NULL,
     CONSTRAINT pk_members PRIMARY KEY (id)
);
