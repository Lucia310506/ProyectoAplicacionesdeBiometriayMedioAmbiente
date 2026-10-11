<?php
/*
 * Fichero: probar_base_datos.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Prueba aislada de la lógica y la base de datos de pruebas.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

putenv('MEDICIONES_ENTORNO=pruebas');
require_once __DIR__ . '/../BBDD/ConexionMediciones.php';
require_once __DIR__ . '/../logica/mediciones.php';

// condicion: B, mensaje: Text --> comprobar() -->
function comprobar(bool $condicion, string $mensaje): void {
    if (!$condicion) { throw new RuntimeException($mensaje); }
}

// --> probarBaseDatos() -->
function probarBaseDatos(): void {
    $conexion = conectarBaseDatos('pruebas');
    try {
        $conexion->exec('DELETE FROM mediciones');
        comprobar((int) $conexion->query('SELECT COUNT(*) FROM mediciones')->fetchColumn() === 0,
            'La tabla de pruebas debe empezar vacía');
        guardarMediciones('CO2', 500);
        guardarMediciones('TEMPERATURA', -19);
        $mediciones = mostrarMediciones();
        comprobar(count($mediciones) === 2, 'Deben existir dos mediciones');
        $porTipo = []; foreach ($mediciones as $medicion) { $porTipo[$medicion['tipo']] = $medicion; }
        comprobar((float) $porTipo['CO2']['valor'] === 500.0, 'CO2 debe valer 500');
        comprobar((float) $porTipo['TEMPERATURA']['valor'] === -19.0, 'Temperatura debe valer -19');
        foreach (['CO2', 'TEMPERATURA'] as $tipo) {
            comprobar(!empty($porTipo[$tipo]['id']), 'Falta el identificador de ' . $tipo);
            comprobar(!empty($porTipo[$tipo]['fecha']), 'La fecha debe generarse en servidor para ' . $tipo);
        }
        echo "OK: pruebas de base de datos superadas\n";
    } finally {
        $conexion->exec('DELETE FROM mediciones');
        comprobar((int) $conexion->query('SELECT COUNT(*) FROM mediciones')->fetchColumn() === 0,
            'La tabla de pruebas debe quedar vacía');
    }
}

probarBaseDatos();
