-- Fichero: datos.sql
-- Autor: Lucía Díaz Murcia
-- Descripción: Inserta datos ficticios de ejemplo en la tabla mediciones.
-- Fecha: 2026-09-25
-- Copyright (c) 2026 Lucía Díaz Murcia
-- Aportación: crear el archivo y hacerlo funcional

INSERT INTO mediciones (dispositivo_id, tipo, valor, fecha, lugar) VALUES
('DISP-CO2', 'CO2', 500, '2026-09-25 10:30:00', 'Murcia'),
('DISP-TEMPERATURA', 'TEMPERATURA', -19, '2026-09-25 10:31:00', 'Murcia');