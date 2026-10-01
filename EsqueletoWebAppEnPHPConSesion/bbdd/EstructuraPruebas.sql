-- Fichero: EstructuraPruebas.sql
-- Autor: Lucía Díaz Murcia
-- Descripción: Crea la base de datos aislada para los tests automáticos.
-- Fecha: 2026-09-25
-- Copyright (c) 2026 Lucía Díaz Murcia

CREATE DATABASE IF NOT EXISTS ldiamur_mediciones_test
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ldiamur_mediciones_test;

CREATE TABLE IF NOT EXISTS mediciones (
  id INT NOT NULL AUTO_INCREMENT,
  tipo ENUM('CO2', 'TEMPERATURA') NOT NULL,
  valor DOUBLE NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
