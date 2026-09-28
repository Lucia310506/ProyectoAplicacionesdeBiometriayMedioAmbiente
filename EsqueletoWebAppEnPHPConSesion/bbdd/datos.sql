-- Fichero: datos.sql
-- Autor: Lucía Díaz Murcia
-- Descripción: Inserta datos ficticios de ejemplo en la tabla mediciones.
-- Fecha: 2026-09-25
-- Copyright (c) 2026 Lucía Díaz Murcia
-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost:3306
-- Tiempo de generación: 25-09-2026 a las 18:19:30
-- Versión del servidor: 10.11.14-MariaDB-0ubuntu0.24.04.1
-- Versión de PHP: 8.4.24

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `mediciones`
--

--
-- Volcado de datos para la tabla `Mediciones`
--

INSERT INTO `mediciones` (`Id`, `Tipo`, `Valor`, `Fecha`) VALUES
(1, 'CO2', 500, '2026-09-25 10:30:00'),
(2, 'TEMPERATURA', -19, '2026-09-25 10:31:00');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
