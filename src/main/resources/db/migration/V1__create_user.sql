CREATE TABLE user_entity (
    id         BIG-SERIAL PRIMARY KEY,
    email      TEXT NOT NULL,
    username   TEXT NOT NULL,
    password   TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);
CREATE UNIQUE INDEX ux_user_entity_email ON user_entity (email);