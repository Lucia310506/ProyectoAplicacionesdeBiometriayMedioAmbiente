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
// Construye la conexión PDO con la configuración resuelta para el entorno.
// --------------------

function conectarBaseDatos(string $entorno = 'produccion'): PDO {
    $configuracion = obtenerConfiguracion($entorno);
    return new PDO(
        "mysql:host={$configuracion['host']};dbname={$configuracion['base']};charset=utf8mb4",
        $configuracion['usuario'],
        $configuracion['password'],
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC]
    );
}

// --------------------
// entorno: Text --> obtenerConfiguracion() --> Dict
// Devuelve credenciales locales o variables del entorno sin publicarlas.
// --------------------

function obtenerConfiguracion(string $entorno = 'produccion'): array {
    if (!in_array($entorno, ['produccion', 'pruebas'], true)) {
        throw new InvalidArgumentException('El entorno debe ser produccion o pruebas');
    }
    $sufijo = $entorno === 'pruebas' ? '_TEST' : '_PROD';
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
    return [
        'host' => $host ?: '127.0.0.1',
        'base' => $base,
        'usuario' => $usuario ?: 'root',
        'password' => $password === false ? '' : $password,
    ];
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
