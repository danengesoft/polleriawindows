-- No modifica filas. Primero crea la protección para categorías no eliminadas.
-- Las categorías inactivas pero no eliminadas siguen reservando su nombre.
CREATE UNIQUE INDEX POLLERIA.UQ_CAT_NOMBRE_VIGENTE ON POLLERIA.CATEGORIAS
(CASE WHEN ELIMINADO=0 THEN UPPER(TRIM(NOMBRE)) ELSE NULL END);
-- Sustituye exclusivamente el índice anterior que reservaba nombres históricos.
DROP INDEX POLLERIA.UQ_CATEGORIAS_NOMBRE;
