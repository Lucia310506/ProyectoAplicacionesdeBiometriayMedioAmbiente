-- Fichero: datos.sql
-- Autor: Lucía Díaz Murcia
-- Descripción: Inserta datos ficticios de ejemplo en la tabla mediciones.
-- Fecha: 2026-09-25
-- Copyright (c) 2026 Lucía Díaz Murcia
-- Aportación: crear el archivo y hacerlo funcional

-- La fecha la genera MySQL para preservar el punto de inserción real.
INSERT INTO mediciones (dispositivo_id, tipo, valor, lugar)
VALUES ('S1', 'CO2', 500, 'Murcia');

INSERT INTO mediciones (dispositivo_id, tipo, valor, lugar)
VALUES ('S2', 'TEMPERATURA', -19, 'Murcia');