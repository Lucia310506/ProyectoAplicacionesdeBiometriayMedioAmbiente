<?php
/*
 * Fichero: probar_base_datos.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Prueba aislada de la lógica y la base de datos de pruebas.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 * Aportación: crear el archivo y hacerlo funcional
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

        foreach ([$dispositivoCo2, $dispositivoTemperatura] as $dispositivo) {
            $stmt = $conexion->prepare('SELECT dispositivo_id, tipo, valor, lugar, DATE_FORMAT(fecha, \'%Y-%m-%dT%H:%i:%s.%f\') AS fecha FROM mediciones WHERE dispositivo_id = :dispositivo_id ORDER BY fecha DESC LIMIT 1');
            $stmt->execute(['dispositivo_id' => $dispositivo]);
            $fila = $stmt->fetch(PDO::FETCH_ASSOC);
            comprobar($fila !== false, 'Debe existir la medición de prueba');
            $medicion = normalizarFilaMedicion($fila);
            comprobar(!empty($medicion['fecha']), 'La fecha debe generarse en servidor');
            comprobar($medicion['lugar'] === 'Murcia', 'Debe conservar el lugar');
        }

        $stmtCo2 = $conexion->prepare('SELECT valor, tipo FROM mediciones WHERE dispositivo_id = :dispositivo_id ORDER BY fecha DESC LIMIT 1');
        $stmtCo2->execute(['dispositivo_id' => $dispositivoCo2]);
        $co2 = $stmtCo2->fetch(PDO::FETCH_ASSOC);
        comprobar($co2 !== false, 'Debe existir la medición CO2 de prueba');
        comprobar((float) $co2['valor'] === 500.0, 'CO2 debe valer 500');
        comprobar($co2['tipo'] === 'CO2', 'Tipo CO2 no coincide');

        $stmtTemp = $conexion->prepare('SELECT valor, tipo FROM mediciones WHERE dispositivo_id = :dispositivo_id ORDER BY fecha DESC LIMIT 1');
        $stmtTemp->execute(['dispositivo_id' => $dispositivoTemperatura]);
        $temp = $stmtTemp->fetch(PDO::FETCH_ASSOC);
        comprobar($temp !== false, 'Debe existir la medición de temperatura de prueba');
        comprobar((float) $temp['valor'] === -19.0, 'Temperatura debe valer -19');
        comprobar($temp['tipo'] === 'TEMPERATURA', 'Tipo temperatura no coincide');

        echo "OK: pruebas de base de datos superadas\n";
    } finally {
        $conexion->prepare('DELETE FROM mediciones WHERE dispositivo_id IN (:co2, :temperatura)')
            ->execute(['co2' => $dispositivoCo2, 'temperatura' => $dispositivoTemperatura]);
    }
}

probarBaseDatos();