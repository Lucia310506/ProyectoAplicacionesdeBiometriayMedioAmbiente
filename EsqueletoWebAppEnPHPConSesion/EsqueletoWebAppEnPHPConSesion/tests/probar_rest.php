<?php
/*
 * Fichero: probar_rest.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Prueba de integración de las rutas REST con la base de pruebas.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

putenv('MEDICIONES_ENTORNO=pruebas');
define('PRUEBA_REST', true);
require_once __DIR__ . '/../BBDD/ConexionMediciones.php';
require_once __DIR__ . '/../rest/mediciones.php';

// condicion: B, mensaje: Text --> comprobarRest() -->
function comprobarRest(bool $condicion, string $mensaje): void {
    if (!$condicion) { throw new RuntimeException($mensaje); }
}

// --> probarRest() -->
function probarRest(): void {
    $conexion = conectarBaseDatos('pruebas');
    try {
        $conexion->exec('DELETE FROM mediciones');
        [$codigoPost, $respuestaPost] = atenderMediciones('POST', '{"tipo":"CO2","valor":500}');
        comprobarRest($codigoPost === 201 && $respuestaPost['resultado'] === 'medición guardada', 'POST debe crear');
        [$codigoGet, $respuestaGet] = atenderMediciones('GET', '');
        comprobarRest($codigoGet === 200 && count($respuestaGet) === 1, 'GET debe devolver la medición');
        comprobarRest($respuestaGet[0]['tipo'] === 'CO2', 'GET debe devolver CO2');
        echo "OK: pruebas REST superadas\n";
    } finally {
        $conexion->exec('DELETE FROM mediciones');
        comprobarRest((int) $conexion->query('SELECT COUNT(*) FROM mediciones')->fetchColumn() === 0,
            'La tabla de pruebas debe quedar vacía');
    }
}

probarRest();
