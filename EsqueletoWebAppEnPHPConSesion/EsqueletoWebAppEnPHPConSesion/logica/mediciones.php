<?php
/*
 * Fichero: mediciones.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Lógica de negocio de las mediciones ambientales.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
//BD: MEDICIONES = [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]
require_once __DIR__ . '/../BBDD/ConexionMediciones.php'; //Busca la carpeta con la conexión

// tipo: Text, valor: R --> guardarMediciones() -->
function guardarMediciones(string $tipo, float $valor): void {
    if (!in_array($tipo, ['CO2', 'TEMPERATURA'], true)) {
        throw new InvalidArgumentException('tipo debe ser CO2 o TEMPERATURA');
    }
    if (!is_finite($valor)) {
        throw new InvalidArgumentException('valor debe ser un número finito');
    }
    $conexion = conectarBaseDatos(obtenerEntornoBaseDatos());
    $sentencia = $conexion->prepare(
        'INSERT INTO mediciones (tipo, valor, fecha) VALUES (:tipo, :valor, NOW())'
    );
    $sentencia->execute(['tipo' => $tipo, 'valor' => $valor]);
}

// mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ] <-- mostrarMediciones() <--
function mostrarMediciones(): array {
    $conexion = conectarBaseDatos(obtenerEntornoBaseDatos());
    $sentencia = $conexion->query(
        "SELECT id, tipo, valor, DATE_FORMAT(fecha, '%Y-%m-%dT%H:%i:%s') AS fecha
         FROM mediciones ORDER BY fecha DESC, id DESC"
    );
    return $sentencia->fetchAll();
}
