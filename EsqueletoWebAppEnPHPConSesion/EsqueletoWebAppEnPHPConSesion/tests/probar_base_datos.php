<?php
/*
 * Fichero: probar_base_datos.php
 * Autor: Lucia
 * Descripción: Pruebas automáticas aisladas de la lógica y la base de datos.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucia
 */

putenv('MEDICIONES_ENTORNO=pruebas');
require_once __DIR__ . '/../BBDD/ConexionMediciones.php';
require_once __DIR__ . '/../logica/mediciones.php';

// condicion: B, mensaje: Text --> comprobar() -->
function comprobar(bool $condicion, string $mensaje): void {
    if (!$condicion) {
        throw new RuntimeException('Fallo: ' . $mensaje);
    }
}

// --> ejecutar_pruebas_base_datos() -->
function ejecutar_pruebas_base_datos(): void {
    $conexion = conectar_mediciones('pruebas');
    try {
        // Esta sentencia solo se ejecuta contra ldiamur_mediciones_test.
        $conexion->query('DELETE FROM Mediciones');
        comprobar((int) $conexion->query('SELECT COUNT(*) AS total FROM Mediciones')
            ->fetch_assoc()['total'] === 0, 'la tabla debe comenzar vacía');

        guardar_mediciones('CO2', 235);
        guardar_mediciones('TEMPERATURA', -12);
        $mediciones = mostrar_mediciones();
        comprobar(count($mediciones) === 2, 'deben existir dos mediciones');
        $porTipo = [];
        foreach ($mediciones as $medicion) {
            $porTipo[$medicion['tipo']] = $medicion;
        }
        comprobar((float) $porTipo['CO2']['valor'] === 235.0, 'CO2 debe valer 235');
        comprobar((float) $porTipo['TEMPERATURA']['valor'] === -12.0,
            'la temperatura debe admitir -12');
        comprobar(!empty($porTipo['CO2']['fecha']), 'la fecha debe generarse en servidor');
        echo "OK: pruebas de base de datos superadas\n";
    } finally {
        $conexion->query('DELETE FROM Mediciones');
        $total = (int) $conexion->query('SELECT COUNT(*) AS total FROM Mediciones')
            ->fetch_assoc()['total'];
        $conexion->close();
        comprobar($total === 0, 'la tabla debe quedar vacía al terminar');
    }
}

ejecutar_pruebas_base_datos();
