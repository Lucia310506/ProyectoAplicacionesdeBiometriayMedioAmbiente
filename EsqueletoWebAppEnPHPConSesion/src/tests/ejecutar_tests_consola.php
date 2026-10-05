<?php
/*
 * Fichero: ejecutar_tests_consola.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Ejecuta tests de lógica y base de datos en el mismo entorno de mediciones de la web.
 * Fecha: 2026-10-03
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

ini_set('display_errors', '0');
header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');

function responder(int $codigo, array $respuesta): void {
    http_response_code($codigo);
    echo json_encode($respuesta, JSON_UNESCAPED_UNICODE);
    exit;
}

if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') {
    responder(405, ['habilitado' => true, 'error' => 'Usa POST para ejecutar las pruebas.']);
}
$origen = $_SERVER['HTTP_ORIGIN'] ?? ($_SERVER['HTTP_REFERER'] ?? '');
$hostOrigen = parse_url($origen, PHP_URL_HOST);
$esquemaOrigen = parse_url($origen, PHP_URL_SCHEME);
$puertoOrigen = parse_url($origen, PHP_URL_PORT);
$hostCabecera = $_SERVER['HTTP_HOST'] ?? '';
$hostSolicitud = parse_url('//' . $hostCabecera, PHP_URL_HOST);
$puertoSolicitud = parse_url('//' . $hostCabecera, PHP_URL_PORT);
$esquemaSolicitud = $_SERVER['HTTP_X_FORWARDED_PROTO'] ?? ($_SERVER['REQUEST_SCHEME'] ?? (!empty($_SERVER['HTTPS']) ? 'https' : 'http'));
$esquemaSolicitud = strtolower(trim(explode(',', $esquemaSolicitud)[0]));
if (!$hostOrigen || !$hostSolicitud
    || strcasecmp($hostOrigen, $hostSolicitud) !== 0
    || strcasecmp((string) $esquemaOrigen, $esquemaSolicitud) !== 0
    || ($puertoOrigen ?: ($esquemaOrigen === 'https' ? 443 : 80))
        !== ($puertoSolicitud ?: ($esquemaSolicitud === 'https' ? 443 : 80))) {
    responder(403, ['habilitado' => true, 'error' => 'La solicitud debe proceder del mismo origen.']);
}

require_once __DIR__ . '/../BBDD/ConexionMediciones.php';
require_once __DIR__ . '/../logica/mediciones.php';

$tests = [];
$conexion = null;
$marcadores = [];
$agregarResultado = static function (string $grupo, string $nombre, callable $prueba) use (&$tests): void {
    try {
        $prueba();
        $tests[] = ['grupo' => $grupo, 'nombre' => $nombre, 'estado' => 'OK'];
    } catch (Throwable $error) {
        $tests[] = ['grupo' => $grupo, 'nombre' => $nombre, 'estado' => 'ERROR', 'detalle' => $error->getMessage()];
    }
};
$comprobar = static function (bool $condicion, string $mensaje): void {
    if (!$condicion) {
        throw new RuntimeException($mensaje);
    }
};
$crearMarcadorUnico = static function (PDO $pdo, string $tipo, bool $negativo): float {
    $consulta = $pdo->prepare('SELECT COUNT(*) FROM mediciones WHERE tipo = :tipo AND valor = :valor');
    do {
        $valor = (float) random_int(2000000000000, 8000000000000);
        if ($negativo) {
            $valor = -$valor;
        }
        $consulta->execute(['tipo' => $tipo, 'valor' => $valor]);
    } while ((int) $consulta->fetchColumn() !== 0);
    return $valor;
};

try {
    $entorno = obtenerEntornoBaseDatos();
    $conexion = conectarBaseDatos($entorno);
    $agregarResultado('baseDatos', 'Conexión y esquema de la base de mediciones', static function () use ($conexion, $comprobar): void {
        $comprobar((int) $conexion->query('SELECT 1')->fetchColumn() === 1, 'La consulta de conexión falló');
        $columnas = $conexion->query('SHOW COLUMNS FROM mediciones')->fetchAll(PDO::FETCH_COLUMN);
        foreach (['id', 'tipo', 'valor', 'fecha'] as $columna) {
            $comprobar(in_array($columna, $columnas, true), 'Falta la columna ' . $columna);
        }
    });

    $marcadores[] = ['tipo' => 'CO2', 'valor' => $crearMarcadorUnico($conexion, 'CO2', false)];
    $marcadores[] = ['tipo' => 'TEMPERATURA', 'valor' => $crearMarcadorUnico($conexion, 'TEMPERATURA', true)];

    $agregarResultado('logica', 'Lógica guarda y devuelve mediciones de prueba', static function () use ($conexion, $marcadores, $comprobar): void {
        foreach ($marcadores as $marcador) {
            guardarMediciones($marcador['tipo'], $marcador['valor']);
        }
        $mediciones = mostrarMediciones();
        foreach ($marcadores as $marcador) {
            $encontrada = false;
            foreach ($mediciones as $medicion) {
                if ($medicion['tipo'] === $marcador['tipo']
                    && (float) $medicion['valor'] === $marcador['valor']
                    && !empty($medicion['id'])
                    && !empty($medicion['fecha'])) {
                    $encontrada = true;
                    break;
                }
            }
            $comprobar($encontrada, 'La lógica no devolvió la medición de prueba ' . $marcador['tipo']);
        }
    });

    $agregarResultado('logica', 'Lógica rechaza tipos no admitidos', static function (): void {
        try {
            guardarMediciones('HUMEDAD', 50.0);
        } catch (InvalidArgumentException $esperado) {
            return;
        }
        throw new RuntimeException('Se debía rechazar el tipo HUMEDAD');
    });
} catch (Throwable $error) {
    $tests[] = ['grupo' => 'baseDatos', 'nombre' => 'Conexión a la base de mediciones', 'estado' => 'ERROR', 'detalle' => $error->getMessage()];
} finally {
    if ($conexion instanceof PDO && count($marcadores) > 0) {
        try {
            $borrar = $conexion->prepare('DELETE FROM mediciones WHERE tipo = :tipo AND valor = :valor');
            $comprobarSinFilas = $conexion->prepare('SELECT COUNT(*) FROM mediciones WHERE tipo = :tipo AND valor = :valor');
            foreach ($marcadores as $marcador) {
                $borrar->execute($marcador);
                $comprobarSinFilas->execute($marcador);
                if ((int) $comprobarSinFilas->fetchColumn() !== 0) {
                    throw new RuntimeException('No se pudo borrar la fila de prueba ' . $marcador['tipo']);
                }
            }
            $tests[] = ['grupo' => 'baseDatos', 'nombre' => 'Limpieza: se borraron solo las filas centinela de prueba', 'estado' => 'OK'];
        } catch (Throwable $error) {
            $tests[] = ['grupo' => 'baseDatos', 'nombre' => 'Limpieza de filas centinela', 'estado' => 'ERROR', 'detalle' => $error->getMessage()];
        }
    }
}

$correctos = count(array_filter($tests, static fn(array $test): bool => $test['estado'] === 'OK'));
responder(200, ['habilitado' => true, 'ok' => $correctos === count($tests), 'tests' => $tests]);



