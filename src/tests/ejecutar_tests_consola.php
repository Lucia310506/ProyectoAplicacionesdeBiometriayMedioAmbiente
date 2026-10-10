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

/*
 * --------------------
 * codigo: N, respuesta: Dict --> responder() -->
 * Serializa el resultado de la batería y termina la petición.
 * --------------------
 */
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

require_once __DIR__ . '/../database/ConexionMediciones.php';
require_once __DIR__ . '/../business_logic/mediciones.php';
define('PRUEBA_REST', true);
require_once __DIR__ . '/../communication/mediciones.php';

$tests = [];
$conexion = null;
$tablaLimpiaAntes = false;
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
try {
    // Esta batería es destructiva por diseño: solo permite ejecutarse contra la BD aislada de pruebas.
    $entorno = getenv('MEDICIONES_ENTORNO');
    if ($entorno !== 'pruebas') {
        throw new RuntimeException('Configura MEDICIONES_ENTORNO=pruebas para ejecutar la batería.');
    }
    $conexion = conectarBaseDatos('pruebas');
    $conexion->exec('DELETE FROM mediciones');
    $tablaLimpiaAntes = true;
    $agregarResultado('baseDatos', 'La tabla de pruebas empieza vacía', static function () use ($conexion, $comprobar): void {
        $comprobar((int) $conexion->query('SELECT COUNT(*) FROM mediciones')->fetchColumn() === 0,
            'La tabla de pruebas no quedó vacía antes de empezar');
    });
    $agregarResultado('baseDatos', 'Conexión y esquema de la base de mediciones', static function () use ($conexion, $comprobar): void {
        $comprobar((int) $conexion->query('SELECT 1')->fetchColumn() === 1, 'La consulta de conexión falló');
        $columnas = $conexion->query('SHOW COLUMNS FROM mediciones')->fetchAll(PDO::FETCH_COLUMN);
        foreach (['id', 'tipo', 'valor', 'fecha'] as $columna) {
            $comprobar(in_array($columna, $columnas, true), 'Falta la columna ' . $columna);
        }
    });
    $agregarResultado('baseDatos', 'Configuración selecciona solo la base de pruebas', static function () use ($comprobar): void {
        $configuracion = obtenerConfiguracion('pruebas');
        $baseEsperada = getenv('MEDICIONES_DB_NAME_TEST') ?: 'ldiamur_mediciones_test';
        $comprobar($configuracion['base'] === $baseEsperada, 'La configuración no seleccionó la base de pruebas');
        try {
            obtenerConfiguracion('desconocido');
        } catch (InvalidArgumentException $error) {
            return;
        }
        throw new RuntimeException('La configuración debe rechazar entornos desconocidos');
    });

    $marcadores = [
        ['tipo' => 'CO2', 'valor' => 500.0],
        ['tipo' => 'TEMPERATURA', 'valor' => -19.0],
    ];

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

    $agregarResultado('rest', 'POST /mediciones devuelve 201 y GET devuelve el alta', static function () use ($conexion, $marcadores, $comprobar): void {
        $valor = 501.0;
        [$codigoPost] = atenderMediciones('POST', json_encode(['tipo' => 'CO2', 'valor' => $valor]));
        $comprobar($codigoPost === 201, 'POST /mediciones debe responder 201');
        [$codigoGet, $respuestaGet] = atenderMediciones('GET', '');
        $comprobar($codigoGet === 200 && is_array($respuestaGet), 'GET /mediciones debe responder una lista');
        $encontrada = false;
        foreach ($respuestaGet as $medicion) {
            if ($medicion['tipo'] === 'CO2' && (float) $medicion['valor'] === $valor) {
                $encontrada = true;
                break;
            }
        }
        $comprobar($encontrada, 'GET no devolvió la medición enviada por POST');
    });
} catch (Throwable $error) {
    $tests[] = ['grupo' => 'baseDatos', 'nombre' => 'Conexión a la base de mediciones', 'estado' => 'ERROR', 'detalle' => $error->getMessage()];
} finally {
    if ($conexion instanceof PDO && $tablaLimpiaAntes) {
        try {
            $conexion->exec('DELETE FROM mediciones');
            $vacias = (int) $conexion->query('SELECT COUNT(*) FROM mediciones')->fetchColumn() === 0;
            $tests[] = ['grupo' => 'baseDatos', 'nombre' => 'Limpieza: tabla de pruebas vacía al terminar', 'estado' => $vacias ? 'OK' : 'ERROR'];
            if (!$vacias) {
                throw new RuntimeException('La tabla de pruebas no quedó vacía al terminar');
            }
        } catch (Throwable $error) {
            $tests[] = ['grupo' => 'baseDatos', 'nombre' => 'Limpieza de la tabla de pruebas', 'estado' => 'ERROR', 'detalle' => $error->getMessage()];
        }
    }
}

$correctos = count(array_filter($tests, static fn(array $test): bool => $test['estado'] === 'OK'));
responder(200, ['habilitado' => true, 'ok' => $correctos === count($tests), 'tests' => $tests]);



