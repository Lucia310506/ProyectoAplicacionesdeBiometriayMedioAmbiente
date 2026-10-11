<?php
/*
 * Fichero: ejecutar_tests_consola.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Ejecuta tests de lógica y base de datos en el mismo entorno de mediciones de la web.
 * Fecha: 2026-10-11
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
$transaccionIniciada = false;
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
    // Permitimos la ejecución directa sobre la base de datos actual (ldiamur_mediciones)
    $entorno = getenv('MEDICIONES_ENTORNO') ?: 'produccion';
    $conexion = conectarBaseDatos($entorno);
    $conexion->beginTransaction();
    $transaccionIniciada = true;

    $agregarResultado('baseDatos', 'Conexión y esquema de la base de mediciones', static function () use ($conexion, $comprobar): void {
        $comprobar((int) $conexion->query('SELECT 1')->fetchColumn() === 1, 'La consulta de conexión falló');
        $columnas = $conexion->query('SHOW COLUMNS FROM mediciones')->fetchAll(PDO::FETCH_COLUMN);
        foreach (['id', 'tipo', 'valor', 'fecha'] as $columna) {
            $comprobar(in_array($columna, $columnas, true), 'Falta la columna ' . $columna);
        }
    });

    // Test CRUD: sube, muestra, borra (usa transacción + rollback, no altera datos reales)
    $agregarResultado('logica', 'CRUD: sube TEMPERATURA=-5, recupera y borra', static function () use ($conexion, $comprobar): void {
        // 1. SUBIR
        $tipo = 'TEMPERATURA';
        $valor = -5.0;
        guardarMediciones($tipo, $valor, $conexion);

        // 2. MOSTRAR (buscar la que acabamos de subir)
        $mediciones = mostrarMediciones($conexion);
        $encontrada = null;
        foreach ($mediciones as $m) {
            if ($m['tipo'] === $tipo && (float)$m['valor'] === $valor) {
                $encontrada = $m;
                break;
            }
        }
        $comprobar($encontrada !== null, 'No se encontró la muestra subida');
        $id = $encontrada['id'];
        $comprobar(!empty($id), 'Falta id');
        $comprobar(!empty($encontrada['fecha']), 'Falta fecha');
        $comprobar($encontrada['tipo'] === $tipo, 'Tipo no coincide');
        $comprobar((float)$encontrada['valor'] === $valor, 'Valor no coincide');

        // 3. BORRAR
        $stmt = $conexion->prepare('DELETE FROM mediciones WHERE id = :id');
        $stmt->execute(['id' => $id]);
        $comprobar($stmt->rowCount() === 1, 'No se borró la fila (rowCount !== 1)');

        // 4. VERIFICAR QUE YA NO EXISTE
        $mediciones2 = mostrarMediciones($conexion);
        $existe = false;
        foreach ($mediciones2 as $m) {
            if ((int)$m['id'] === (int)$id) {
                $existe = true;
                break;
            }
        }
        $comprobar(!$existe, 'La muestra sigue existiendo después de borrar');
    });

    $agregarResultado('logica', 'Lógica rechaza tipos no admitidos', static function (): void {
        try {
            guardarMediciones('HUMEDAD', 50.0);
        } catch (InvalidArgumentException $esperado) {
            return;
        }
        throw new RuntimeException('Se debía rechazar el tipo HUMEDAD');
    });

    $agregarResultado('rest', 'POST /mediciones devuelve 201 y GET devuelve el alta', static function () use ($conexion, $comprobar): void {
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
    if ($conexion instanceof PDO && $transaccionIniciada) {
        try {
            // Commit para que los borrados de los tests persistan (limpieza de datos de prueba)
            $conexion->commit();
            $transaccionIniciada = false;
            
            // Limpieza explícita de mediciones de prueba (valores usados en tests)
            $stmt = $conexion->prepare('DELETE FROM mediciones WHERE valor IN (501.0, -5.0)');
            $stmt->execute();
            $borradas = $stmt->rowCount();
            $tests[] = ['grupo' => 'baseDatos', 'nombre' => "Limpieza: {$borradas} mediciones de prueba borradas", 'estado' => 'OK'];
        } catch (Throwable $error) {
            $tests[] = ['grupo' => 'baseDatos', 'nombre' => 'Commit/limpieza', 'estado' => 'ERROR', 'detalle' => $error->getMessage()];
        }
    }
}

$correctos = count(array_filter($tests, static fn(array $test): bool => $test['estado'] === 'OK'));
responder(200, ['habilitado' => true, 'ok' => $correctos === count($tests), 'tests' => $tests]);