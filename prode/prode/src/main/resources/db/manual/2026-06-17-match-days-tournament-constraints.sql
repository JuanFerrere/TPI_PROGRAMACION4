-- Script manual para ejecutar una sola vez si la base conserva la unicidad global de match_days.name.
--
-- Contexto:
-- - La entidad MatchDay ya no usa name unico global porque las fechas ahora pueden repetirse en torneos distintos.
-- - spring.jpa.hibernate.ddl-auto=update no suele eliminar constraints antiguas.
-- - Este script reemplaza la unicidad global por indices unicos parciales por tournament_id.
--
-- Ejecutar contra PostgreSQL despues de desplegar el modelo que agrega:
-- - match_days.tournament_id
-- - match_days.order_number

ALTER TABLE match_days DROP CONSTRAINT IF EXISTS uk_match_days_name;
ALTER TABLE match_days DROP CONSTRAINT IF EXISTS match_days_name_key;

CREATE UNIQUE INDEX IF NOT EXISTS uk_match_days_tournament_name
	ON match_days (tournament_id, lower(name))
	WHERE tournament_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_match_days_tournament_order_number
	ON match_days (tournament_id, order_number)
	WHERE tournament_id IS NOT NULL
	AND order_number IS NOT NULL;
