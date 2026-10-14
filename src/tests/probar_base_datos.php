<?php
/*
 * Fichero: probar_base_datos.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Prueba aislada de la lógica y la base de datos de pruebas.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

putenv('MEDICIONES_ENTORNO=pruebas');
require_once __DIR__ . '/../database/ConexionMediciones.php';
require_once __DIR__ . '/../business_logic/mediciones.php';

// --------------------
// condicion: B, mensaje: Text --> comprobar() -->
// Detiene el caso con un error legible si la condición no se cumple.
// --------------------
function comprobar(bool $condicion, string $mensaje): void {
    if (!$condicion) { throw new RuntimeException($mensaje); }
}

// --------------------
// --> probarBaseDatos() -->
// Comprueba inserción, consulta y normalización sin borrar datos ajenos.
// --------------------
function probarBaseDatos(): void {
    $conexion = conectarBaseDatos('pruebas');
    $dispositivoCo2 = 'TEST-BASE-CO2';
    $dispositivoTemperatura = 'TEST-BASE-TEMPERATURA';
    try {
        $conexion->prepare('DELETE FROM mediciones WHERE dispositivo_id IN (:co2, :temperatura)')
            ->execute(['co2' => $dispositivoCo2, 'temperatura' => $dispositivoTemperatura]);
        guardarMediciones($dispositivoCo2, 'CO2', 500, 'Murcia', $conexion);
        guardarMediciones($dispositivoTemperatura, 'TEMPERATURA', -19, 'Murcia', $conexion);
        $mediciones = mostrarMediciones($conexion);
        $porTipo = [];
        foreach ($mediciones as $medicion) {
            if (in_array($medicion['dispositivo_id'], [$dispositivoCo2, $dispositivoTemperatura], true)) {
                $porTipo[$medicion['dispositivo_id']] = $medicion;
            }
        }
        comprobar(isset($porTipo[$dispositivoCo2]), 'Debe existir la medición CO2 de prueba');
        comprobar(isset($porTipo[$dispositivoTemperatura]), 'Debe existir la medición de temperatura de prueba');
        comprobar((float) $porTipo[$dispositivoCo2]['valor'] === 500.0, 'CO2 debe valer 500');
        comprobar((float) $porTipo[$dispositivoTemperatura]['valor'] === -19.0, 'Temperatura debe valer -19');
        comprobar($porTipo[$dispositivoCo2]['tipo'] === 'CO2', 'Tipo CO2 no coincide');
        comprobar($porTipo[$dispositivoTemperatura]['tipo'] === 'TEMPERATURA', 'Tipo temperatura no coincide');
        comprobar(!empty($porTipo[$dispositivoCo2]['fecha']), 'La fecha debe generarse en servidor para CO2');
        comprobar(!empty($porTipo[$dispositivoTemperatura]['fecha']), 'La fecha debe generarse en servidor para TEMPERATURA');
        comprobar($porTipo[$dispositivoCo2]['lugar'] === 'Murcia', 'CO2 debe conservar el lugar');
        echo "OK: pruebas de base de datos superadas\n";
    } finally {
        $conexion->prepare('DELETE FROM mediciones WHERE dispositivo_id IN (:co2, :temperatura)')
            ->execute(['co2' => $dispositivoCo2, 'temperatura' => $dispositivoTemperatura]);
    }
}

probarBaseDatos();