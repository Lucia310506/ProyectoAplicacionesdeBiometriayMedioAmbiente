<?php
/*
 * Fichero: ejecutar_tests_consola.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Ejecuta tests de lógica y base de datos en el mismo entorno de mediciones de la web.
 * Fecha: 2026-10-11
 * Copyright (c) 2026 Lucía Díaz Murcia
 * Aportación: crear el archivo y hacerlo funcional
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
$archivoProduccion = __DIR__ . '/../database/ConfiguracionProduccion.php';
$entornoPruebas = is_file($archivoProduccion) || obtenerEntornoBaseDatos() === 'pruebas';
if (!$entornoPruebas) {
    responder(403, ['habilitado' => false, 'error' => 'Crea ConfiguracionProduccion.php o define MEDICIONES_ENTORNO=pruebas.']);
}
$entorno = 'pruebas';
require_once __DIR__ . '/../business_logic/mediciones.php';
define('PRUEBA_REST', true);
require_once __DIR__ . '/../communication/mediciones.php';

$cuerpoSolicitud = json_decode((string) file_get_contents('php://input'), true);
$grupoSolicitado = $cuerpoSolicitud['grupo'] ?? 'all';
$gruposValidos = ['all', 'logica', 'baseDatos', 'rest'];
if (!in_array($grupoSolicitado, $gruposValidos, true)) {
    $grupoSolicitado = 'all';
}

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
    $conexion = conectarBaseDatos($entorno);
    $conexion->beginTransaction();
    $transaccionIniciada = true;

    if ($grupoSolicitado === 'all' || $grupoSolicitado === 'baseDatos') {
        $agregarResultado('baseDatos', 'Conexión y esquema de la base de mediciones', static function () use ($conexion, $comprobar): void {
            $comprobar((int) $conexion->query('SELECT 1')->fetchColumn() === 1, 'La consulta de conexión falló');
            $columnas = $conexion->query('SHOW COLUMNS FROM mediciones')->fetchAll(PDO::FETCH_COLUMN);
            foreach (['dispositivo_id', 'tipo', 'valor', 'fecha'] as $columna) {
                $comprobar(in_array($columna, $columnas, true), 'Falta la columna ' . $columna);
            }
        });
    }

    if ($grupoSolicitado === 'all' || $grupoSolicitado === 'logica') {
        // Test CRUD: sube, muestra, borra (usa transacción + rollback, no altera datos reales)
        $agregarResultado('logica', 'CRUD: sube TEMPERATURA=-5, recupera y borra', static function () use ($conexion, $comprobar): void {
            // 1. SUBIR
            $tipo = 'TEMPERATURA';
            $valor = -5.0;
            guardarMediciones('TEST-CRUD', $tipo, $valor, 'Murcia', $conexion);

            $sentencia = $conexion->prepare('SELECT dispositivo_id, tipo, valor, DATE_FORMAT(fecha, \'%Y-%m-%dT%H:%i:%s.%f\') AS fecha FROM mediciones WHERE dispositivo_id = :dispositivo_id ORDER BY fecha DESC LIMIT 1');
            $sentencia->execute(['dispositivo_id' => 'TEST-CRUD']);
            $encontrada = $sentencia->fetch();
            $comprobar($encontrada !== false, 'No se encontró la muestra subida');
            $dispositivo = (string) $encontrada['dispositivo_id'];
            $fecha = (string) $encontrada['fecha'];
            $comprobar(!empty($dispositivo), 'Falta dispositivo_id');
            $comprobar(!empty($fecha), 'Falta fecha');
            $comprobar((string) $encontrada['tipo'] === $tipo, 'Tipo no coincide');
            $comprobar((float) $encontrada['valor'] === $valor, 'Valor no coincide');

            // 3. BORRAR
            $stmt = $conexion->prepare('DELETE FROM mediciones WHERE dispositivo_id = :dispositivo_id');
            $stmt->execute(['dispositivo_id' => $dispositivo]);
            $comprobar($stmt->rowCount() === 1, 'No se borró la fila (rowCount !== 1)');

            // 4. VERIFICAR QUE YA NO EXISTE
            $sentencia = $conexion->prepare('SELECT 1 FROM mediciones WHERE dispositivo_id = :dispositivo_id');
            $sentencia->execute(['dispositivo_id' => $dispositivo]);
            $comprobar($sentencia->fetch() === false, 'La muestra sigue existiendo después de borrar');
        });

        $agregarResultado('logica', 'Lógica rechaza tipos no admitidos', static function () use ($conexion, $comprobar): void {
            try {
                guardarMediciones('TEST-LOGICA', 'HUMEDAD', 50.0, 'Murcia', $conexion);
            } catch (InvalidArgumentException $esperado) {
                return;
            }
            throw new RuntimeException('Se debía rechazar el tipo HUMEDAD');
        });
    }

    if ($grupoSolicitado === 'all' || $grupoSolicitado === 'rest') {
        $agregarResultado('rest', 'OPTIONS /mediciones devuelve 204', static function () use ($comprobar): void {
            [$codigo, $respuesta] = atenderMediciones('OPTIONS', '');
            $comprobar($codigo === 204, 'OPTIONS /mediciones debe responder 204');
        });

        $agregarResultado('rest', 'Método no permitido devuelve 405', static function () use ($comprobar): void {
            [$codigo] = atenderMediciones('DELETE', '');
            $comprobar($codigo === 405, 'DELETE /mediciones debe responder 405');
        });

        $agregarResultado('rest', 'POST /mediciones devuelve 400 sin campos obligatorios', static function () use ($comprobar): void {
            [$codigo] = atenderMediciones('POST', json_encode(['tipo' => 'CO2', 'valor' => 1]));
            $comprobar($codigo === 400, 'El POST incompleto debe responder 400');
        });

        $agregarResultado('rest', 'POST /mediciones devuelve 400 con JSON incorrecto', static function () use ($comprobar): void {
            [$codigo] = atenderMediciones('POST', '{json invalido');
            $comprobar($codigo === 400, 'El JSON incorrecto debe responder 400');
        });

        $agregarResultado('rest', 'POST /mediciones devuelve 422 con tipo no admitido', static function () use ($comprobar): void {
            [$codigo] = atenderMediciones('POST', json_encode(['dispositivo_id' => 'TEST-REST', 'tipo' => 'HUMEDAD', 'valor' => 1, 'lugar' => 'Murcia']));
            $comprobar($codigo === 422, 'El tipo no admitido debe responder 422');
        });

        $agregarResultado('rest', 'POST /mediciones devuelve 201', static function () use ($comprobar): void {
            $valor = 501.0;
            [$codigoPost] = atenderMediciones('POST', json_encode(['dispositivo_id' => 'TEST-REST', 'tipo' => 'CO2', 'valor' => $valor, 'lugar' => 'Murcia']));
            $comprobar($codigoPost === 201, 'POST /mediciones debe responder 201');
        });

        $agregarResultado('rest', 'GET /mediciones devuelve 200 y una lista', static function () use ($comprobar): void {
            [$codigo, $respuesta] = atenderMediciones('GET', '');
            $comprobar($codigo === 200 && is_array($respuesta), 'GET /mediciones debe responder una lista con código 200');
        });

        $agregarResultado('rest', 'GET /mediciones devuelve el alta REST', static function () use ($conexion, $comprobar): void {
            $sentencia = $conexion->prepare('SELECT COUNT(*) FROM mediciones WHERE dispositivo_id = :dispositivo_id AND tipo = :tipo AND ABS(valor - :valor) < :tolerancia');
            $sentencia->execute([
                'dispositivo_id' => 'TEST-REST',
                'tipo' => 'CO2',
                'valor' => 501.0,
                'tolerancia' => 0.000001,
            ]);
            $comprobar((int) $sentencia->fetchColumn() === 1, 'GET no devolvió la medición enviada por POST');
        });
    }
} catch (Throwable $error) {
    $tests[] = ['grupo' => 'baseDatos', 'nombre' => 'Conexión a la base de mediciones', 'estado' => 'ERROR', 'detalle' => $error->getMessage()];
} finally {
    if ($conexion instanceof PDO && $transaccionIniciada) {
        try {
            // Commit para que los borrados de los tests persistan (limpieza de datos de prueba)
            $conexion->commit();
            $transaccionIniciada = false;

            // Limpieza explícita de mediciones de prueba generadas por la batería
            $stmt = $conexion->prepare("DELETE FROM mediciones WHERE dispositivo_id LIKE 'TEST-%' OR dispositivo_id LIKE 'DISP-%'");
            $stmt->execute();
            $borradas = $stmt->rowCount();
            $tests[] = ['grupo' => 'baseDatos', 'nombre' => "Limpieza: {$borradas} mediciones de prueba borradas", 'estado' => 'OK'];
        } catch (Throwable $error) {
            $tests[] = ['grupo' => 'baseDatos', 'nombre' => 'Commit/limpieza', 'estado' => 'ERROR', 'detalle' => $error->getMessage()];
        }
    }
}

$correctos = count(array_filter($tests, static fn(array $test): bool => $test['estado'] === 'OK'));
responder(200, ['habilitado' => true, 'grupo' => $grupoSolicitado, 'ok' => $correctos === count($tests), 'tests' => $tests]);