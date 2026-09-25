<?php
/*
 * Fichero: mediciones.php
 * Autor: Lucia
 * Descripción: Lógica de negocio para guardar y mostrar mediciones ambientales.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucia
 */

require_once __DIR__ . '/../BBDD/ConexionMediciones.php';

// tipo: Text, valor: R --> guardar_mediciones() -->
function guardar_mediciones(string $tipo, float $valor): void {
    if (!in_array($tipo, ['CO2', 'TEMPERATURA'], true)) {
        throw new InvalidArgumentException('tipo debe ser CO2 o TEMPERATURA');
    }
    if (!is_finite($valor)) {
        throw new InvalidArgumentException('valor debe ser un número finito');
    }
    if ($tipo === 'CO2' && $valor < 0) {
        throw new InvalidArgumentException('el valor de CO2 no puede ser negativo');
    }

    $conexion = conectar_mediciones(getenv('MEDICIONES_ENTORNO') ?: 'produccion');
    $sentencia = $conexion->prepare(
        'INSERT INTO Mediciones (Tipo, Valor, Fecha) VALUES (?, ?, NOW())'
    );
    $sentencia->bind_param('sd', $tipo, $valor);
    $sentencia->execute();
    $sentencia->close();
    $conexion->close();
}

// mediciones: [ (id: N, tipo: Text, valor: R, fecha: R) ] <-- mostrar_mediciones() <--
function mostrar_mediciones(): array {
    $conexion = conectar_mediciones(getenv('MEDICIONES_ENTORNO') ?: 'produccion');
    $resultado = $conexion->query(
        'SELECT Id AS id, Tipo AS tipo, Valor AS valor, Fecha AS fecha
         FROM Mediciones ORDER BY Fecha DESC, Id DESC'
    );
    $mediciones = $resultado->fetch_all(MYSQLI_ASSOC);
    $resultado->free();
    $conexion->close();
    return $mediciones;
}
