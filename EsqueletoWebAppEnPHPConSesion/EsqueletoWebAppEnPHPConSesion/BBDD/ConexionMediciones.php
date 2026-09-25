<?php
/*
 * Fichero: ConexionMediciones.php
 * Autor: Lucia
 * Descripción: Crea conexiones MySQL para las mediciones según el entorno.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucia
 */

// entorno: Text --> conectar_mediciones() --> mysqli
function conectar_mediciones(string $entorno = 'produccion'): mysqli {
    $sufijo = $entorno === 'pruebas' ? '_TEST' : '';
    $host = getenv('MEDICIONES_DB_HOST' . $sufijo) ?: '127.0.0.1';
    $usuario = getenv('MEDICIONES_DB_USER' . $sufijo) ?: 'root';
    $password = getenv('MEDICIONES_DB_PASSWORD' . $sufijo) ?: '';
    $baseDeDatos = getenv('MEDICIONES_DB_NAME' . $sufijo)
        ?: ($entorno === 'pruebas' ? 'ldiamur_mediciones_test' : 'mediciones');

    mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);
    $conexion = new mysqli($host, $usuario, $password, $baseDeDatos);
    $conexion->set_charset('utf8mb4');
    return $conexion;
}
