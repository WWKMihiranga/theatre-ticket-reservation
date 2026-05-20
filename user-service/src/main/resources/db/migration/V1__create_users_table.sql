-- Users table: stores accounts. Maps to coursework "Person" (NIC, name, surname, email)
-- but adds password and role for real-world auth.
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    nic             VARCHAR(20)  NOT NULL UNIQUE,
    name            VARCHAR(80)  NOT NULL,
    surname         VARCHAR(80)  NOT NULL,
    email           VARCHAR(160) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_nic   ON users(nic);
