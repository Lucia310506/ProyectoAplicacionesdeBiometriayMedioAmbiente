<?php
/*
 * Fichero: ConexionMediciones.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Configura conexiones PDO para desarrollo, producción y pruebas.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

// entorno: Text --> conectarBaseDatos() --> PDO
function conectarBaseDatos(string $entorno = 'produccion'): PDO {
    $sufijo = $entorno === 'pruebas' ? '_TEST' : ($entorno === 'produccion' ? '_PROD' : '');
    $configuracion = [];
    if ($entorno === 'produccion') {
        $rutaConfiguracion = __DIR__ . '/ConfiguracionProduccion.php';
        if (is_file($rutaConfiguracion)) {
            $configuracion = require $rutaConfiguracion;
        }
    }
    $host = $configuracion['host'] ?? getenv('MEDICIONES_DB_HOST' . $sufijo);
    $base = getenv('MEDICIONES_DB_NAME' . $sufijo)
        ?: ($configuracion['base'] ?? ($entorno === 'pruebas' ? 'ldiamur_mediciones_test' : 'mediciones'));
    $usuario = $configuracion['usuario'] ?? getenv('MEDICIONES_DB_USER' . $sufijo);
    $password = $configuracion['password'] ?? getenv('MEDICIONES_DB_PASSWORD' . $sufijo);
    if ($entorno === 'produccion' && (!$host || !$usuario || !$password
        || str_starts_with($base, 'ESCRIBE_') || str_starts_with($usuario, 'ESCRIBE_'))) {
        throw new RuntimeException('Falta configurar la conexión de producción en Plesk');
    }
    $host = $host ?: '127.0.0.1';
    $usuario = $usuario ?: 'root';
    $password = $password === false ? '' : $password;
    return new PDO("mysql:host={$host};dbname={$base};charset=utf8mb4", $usuario, $password,
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC]);
}

// --> obtenerEntornoBaseDatos() <--
function obtenerEntornoBaseDatos(): string {
    return getenv('MEDICIONES_ENTORNO') ?: 'produccion';
}
