-- Fichero: Estructura.sql
-- Autor: Lucía Díaz Murcia
-- Descripción: Estructura de la tabla MEDICIONES según el diseño obligatorio.
-- Fecha: 2026-09-25
-- Copyright (c) 2026 Lucía Díaz Murcia

CREATE TABLE IF NOT EXISTS mediciones (
  dispositivo_id VARCHAR(64) NOT NULL,
  fecha DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  tipo ENUM('CO2', 'TEMPERATURA') NOT NULL,
  valor DOUBLE NOT NULL,
  lugar VARCHAR(100) NOT NULL DEFAULT 'Ubicación no disponible',
  PRIMARY KEY (dispositivo_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;