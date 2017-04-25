# --- !Ups

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username TEXT NOT NULL UNIQUE
);

INSERT INTO users(id, username) VALUES(1, 'DEMO');

CREATE TABLE messages (
  id SERIAL PRIMARY KEY,
  created_by INTEGER NOT NULL REFERENCES users(id) DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  display_from TIMESTAMP NOT NULL,
  display_until TIMESTAMP,
  message TEXT NOT NULL
);

# --- !Downs

DROP TABLE messages;
DROP TABLE users;
