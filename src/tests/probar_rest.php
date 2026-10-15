<?php
/*
 * Fichero: probar_rest.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Prueba de integración de las rutas REST con la base de pruebas.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 * Aportación: crear el archivo y hacerlo funcional
 */

putenv('MEDICIONES_ENTORNO=pruebas');
define('PRUEBA_REST', true);
require_once __DIR__ . '/../database/ConexionMediciones.php';
require_once __DIR__ . '/../communication/mediciones.php';

// --------------------
// condicion: B, mensaje: Text --> comprobarRest() -->
// --------------------
function comprobarRest(bool $condicion, string $mensaje): void {
    if (!$condicion) { throw new RuntimeException($mensaje); }
}

// --------------------
// --> probarRest() -->
// --------------------
function probarRest(): void {
    $conexion = conectarBaseDatos('pruebas');
    try {
        $conexion->prepare('DELETE FROM mediciones WHERE dispositivo_id = :dispositivo')
            ->execute(['dispositivo' => 'TEST-REST']);
        [$codigoPost, $respuestaPost] = atenderMediciones('POST', '{"dispositivo_id":"TEST-REST","tipo":"CO2","valor":500,"lugar":"Murcia"}');
        comprobarRest($codigoPost === 201 && $respuestaPost['resultado'] === 'medición guardada', 'POST debe crear');
        [$codigoGet, $respuestaGet] = atenderMediciones('GET', '');
        $restEncontrada = false;
        foreach ($respuestaGet as $medicion) {
            if ($medicion['dispositivo_id'] === 'TEST-REST' && $medicion['tipo'] === 'CO2'
                && (float) $medicion['valor'] === 500.0) {
                $restEncontrada = true;
                break;
            }
        }
        comprobarRest($codigoGet === 200 && $restEncontrada, 'GET debe devolver la medición de prueba');
        echo "OK: pruebas REST superadas\n";
    } finally {
        $conexion->prepare('DELETE FROM mediciones WHERE dispositivo_id = :dispositivo')
            ->execute(['dispositivo' => 'TEST-REST']);
    }
}

probarRest();
