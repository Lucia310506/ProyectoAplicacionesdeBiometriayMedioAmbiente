<?php
/*
 * Fichero: ConexionMediciones.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Configura conexiones PDO para desarrollo, producción y pruebas.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 * Aportación: crear el archivo y hacerlo funcional
 */



// --------------------
// nombre: Text --> variableEntorno() --> Texto|Falso
// Lee una variable desde getenv, $_ENV o $_SERVER para compatibilidad con Apache y PHP-FPM/Plesk.
// --------------------
function variableEntorno(string $nombre) {
    $valor = getenv($nombre);
    if ($valor === false && isset($_ENV[$nombre])) {
        $valor = $_ENV[$nombre];
    }
    if ($valor === false && isset($_SERVER[$nombre])) {
        $valor = $_SERVER[$nombre];
    }
    return $valor;
}
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
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC] //dEVUELVE EXCEPCIONES Y ARRAYS
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
    $rutaConfiguracion = __DIR__ . '/ConfiguracionProduccion.php';
    if (is_file($rutaConfiguracion)) {
        $configuracion = require $rutaConfiguracion;
    }
    if ($configuracion) {
        // ConfiguracionProduccion.php no debe ser sobrescrita por variables rancias del entorno.
        $host = $configuracion['host'] ?? null;
        $base = $configuracion['base'] ?? ($entorno === 'pruebas' ? 'ldiamur_mediciones_test' : 'mediciones');
        $usuario = array_key_exists('usuario', $configuracion) ? $configuracion['usuario'] : null;
        $password = array_key_exists('password', $configuracion) ? $configuracion['password'] : null;
    } else {
        $host = variableEntorno('MEDICIONES_DB_HOST' . $sufijo);
        $base = variableEntorno('MEDICIONES_DB_NAME' . $sufijo)
            ?: ($entorno === 'pruebas' ? 'ldiamur_mediciones_test' : 'mediciones');
        $usuario = variableEntorno('MEDICIONES_DB_USER' . $sufijo);
        $password = variableEntorno('MEDICIONES_DB_PASSWORD' . $sufijo);
    }
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
    if (is_file(__DIR__ . '/ConfiguracionPruebas.php')) {
        return 'pruebas';
    }
    $valor = variableEntorno('MEDICIONES_ENTORNO');
    return $valor !== false && $valor !== '' ? $valor : 'produccion';
}
