<?php
/*
 * Fichero: mediciones.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Lógica de negocio de las mediciones ambientales.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
//BD: MEDICIONES = [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]
require_once __DIR__ . '/../database/ConexionMediciones.php';

// --------------------
// tipo: Text, valor: R --> guardarMediciones() -->
// Valida la medición y la inserta usando una consulta preparada.
// --------------------

function guardarMediciones(string $tipo, float $valor): void {
    if (!in_array($tipo, ['CO2', 'TEMPERATURA'], true)) {
        throw new InvalidArgumentException('tipo debe ser CO2 o TEMPERATURA');
    }
    if (!is_finite($valor)) {
        throw new InvalidArgumentException('valor debe ser un número finito');
    }
    $conexion = conectarBaseDatos(obtenerEntornoBaseDatos());
    $parametros = ['tipo' => $tipo, 'valor' => $valor];
    $consultasInsert = [
        'INSERT INTO mediciones (tipo, valor, fecha) VALUES (:tipo, :valor, NOW())',
        'INSERT INTO mediciones (Tipo, Valor, Fecha) VALUES (:tipo, :valor, NOW())',
        'INSERT INTO Mediciones (tipo, valor, fecha) VALUES (:tipo, :valor, NOW())',
        'INSERT INTO Mediciones (Tipo, Valor, Fecha) VALUES (:tipo, :valor, NOW())',
    ];
    ejecutarPrimeraConsultaValida($conexion, $consultasInsert, $parametros);
}

// --------------------
// mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ] <-- mostrarMediciones() <--
// Lee y normaliza las filas para devolver el contrato compartido.
// --------------------

function mostrarMediciones(): array {
    $conexion = conectarBaseDatos(obtenerEntornoBaseDatos());
    $consultasSelect = [
        "SELECT id AS id, tipo AS tipo, valor AS valor,
                DATE_FORMAT(fecha, '%Y-%m-%dT%H:%i:%s') AS fecha
         FROM mediciones ORDER BY fecha DESC, id DESC",
        "SELECT Id AS id, Tipo AS tipo, Valor AS valor,
                DATE_FORMAT(Fecha, '%Y-%m-%dT%H:%i:%s') AS fecha
         FROM mediciones ORDER BY Fecha DESC, Id DESC",
        "SELECT Id AS id, Tipo AS tipo, Valor AS valor,
                DATE_FORMAT(Fecha, '%Y-%m-%dT%H:%i:%s') AS fecha
         FROM Mediciones ORDER BY Fecha DESC, Id DESC",
    ];
    $filas = ejecutarPrimeraConsultaValida($conexion, $consultasSelect, null);
    $mediciones = [];
    foreach ($filas as $fila) {
        $mediciones[] = normalizarFilaMedicion($fila);
    }
    return $mediciones;
}

// --------------------
// conexion: PDO, consultas: [Text], parametros: Dict|Nulo --> ejecutarPrimeraConsultaValida() --> [Dict]
// Ejecuta la primera variante compatible con el esquema desplegado.
// --------------------

function ejecutarPrimeraConsultaValida(PDO $conexion, array $consultas, ?array $parametros): array {
    $ultimoError = null;
    foreach ($consultas as $consulta) {
        try {
            $sentencia = $conexion->prepare($consulta);
            $sentencia->execute($parametros ?? []);
            return $parametros === null ? $sentencia->fetchAll() : [];
        } catch (PDOException $error) {
            $ultimoError = $error;
        }
    }
    throw $ultimoError ?? new RuntimeException('No se pudo ejecutar la consulta de mediciones');
}

// --------------------
// fila: Dict --> normalizarFilaMedicion() --> (id: N, tipo: Text, valor: R, fecha: DateTime)
// Normaliza tipos y nombres de columna antes de entregar una medición.
// --------------------

function normalizarFilaMedicion(array $fila): array {
    return [
        'id' => (int) ($fila['id'] ?? $fila['Id'] ?? 0),
        'tipo' => (string) ($fila['tipo'] ?? $fila['Tipo'] ?? ''),
        'valor' => (float) ($fila['valor'] ?? $fila['Valor'] ?? 0),
        'fecha' => (string) ($fila['fecha'] ?? $fila['Fecha'] ?? ''),
    ];
}
