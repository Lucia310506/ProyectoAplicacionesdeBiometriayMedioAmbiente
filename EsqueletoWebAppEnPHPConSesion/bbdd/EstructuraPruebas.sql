-- Fichero: EstructuraPruebas.sql
-- Autor: Lucia
-- Descripción: Crea la base de datos aislada para los tests automáticos.
-- Fecha: 2026-09-25
-- Copyright (c) 2026 Lucia

CREATE DATABASE IF NOT EXISTS ldiamur_mediciones_test
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ldiamur_mediciones_test;

CREATE TABLE IF NOT EXISTS mediciones (
  Id INT NOT NULL AUTO_INCREMENT,
  Tipo ENUM('CO2', 'TEMPERATURA') NOT NULL,
  Valor DOUBLE NOT NULL,
  Fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (Id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
