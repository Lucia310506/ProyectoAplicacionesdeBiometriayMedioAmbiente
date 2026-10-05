<?php
/*
 * Fichero: ConexionMediciones.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Configura conexiones PDO para desarrollo, producción y pruebas.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

// --------------------
// entorno: Text --> conectarBaseDatos() --> PDO
// Resuelve credenciales del entorno y construye la conexión PDO.
// --------------------

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
    if ($entorno === 'produccion' && credencialesProduccionIncompletas($host, $base, $usuario, $password)) {
        throw new RuntimeException(
            'Falta ConfiguracionProduccion.php en Plesk. Copia ConfiguracionProduccion.ejemplo.php y escribe host, base, usuario y password.'
        );
    }
    $host = $host ?: '127.0.0.1';
    $usuario = $usuario ?: 'root';
    $password = $password === false ? '' : $password;
    return new PDO("mysql:host={$host};dbname={$base};charset=utf8mb4", $usuario, $password,
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC]);
}

// --------------------
// host: Text, base: Text, usuario: Text, password: Text --> credencialesProduccionIncompletas() --> B
// Detecta credenciales ausentes o marcadores de plantilla antes de conectar.
// --------------------

function credencialesProduccionIncompletas($host, $base, $usuario, $password): bool {
    if (!$host || !$usuario || $password === false || $password === null || $password === '') {
        return true;
    }
    foreach ([$base, $usuario, $password] as $valor) {
        if (is_string($valor) && str_starts_with($valor, 'ESCRIBE_')) {
            return true;
        }
    }
    return false;
}

// --------------------
// --> obtenerEntornoBaseDatos() --> Text
// Devuelve el entorno seleccionado o producción por defecto.
// --------------------

function obtenerEntornoBaseDatos(): string {
    return getenv('MEDICIONES_ENTORNO') ?: 'produccion';
}
