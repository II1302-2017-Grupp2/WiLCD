# --- !Ups

CREATE INDEX messages_by_diplay_from
  ON messages (display_from DESC, occurrence);
CREATE INDEX messages_by_diplay_from_reverse
  ON messages (display_from ASC, display_until);

# --- !Downs

DROP INDEX messages_by_diplay_from_reverse;
DROP INDEX messages_by_diplay_from;
