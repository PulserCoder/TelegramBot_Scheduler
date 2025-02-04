-- liquibase formatted sql


-- changeset pkuzmin:1
CREATE TABLE notification_task (
    id        SERIAL PRIMARY KEY,
    chat_id   BIGINT NOT NULL ,
    text      VARCHAR(255) NOT NULL,
    date_time TIMESTAMP NOT NULL
)