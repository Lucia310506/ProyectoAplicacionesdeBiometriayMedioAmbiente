-- Fichero: normalizar_columnas.sql
-- Autor: Lucía Díaz Murcia
-- Descripción: Adapta una tabla Plesk con columnas Id/Tipo/Valor/Fecha al diseño en minúsculas.
-- Fecha: 2026-09-25
-- Copyright (c) 2026 Lucía Díaz Murcia
-- Ejecutar en phpMyAdmin solo si la tabla ya existe con nombres en PascalCase.

ALTER TABLE mediciones
  CHANGE `Id` `id` INT NOT NULL AUTO_INCREMENT,
  CHANGE `Tipo` `tipo` ENUM('CO2','TEMPERATURA') NOT NULL,
  CHANGE `Valor` `valor` DOUBLE NOT NULL,
  CHANGE `Fecha` `fecha` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
