<?php
/*
 * Fichero: mediciones.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Lógica de negocio de las mediciones ambientales.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 * Aportación: crear el archivo y hacerlo funcional
 */
//BD: MEDICIONES = [ (dispositivo_id: Text, fecha: DateTime, tipo: Text, valor: R) ]
require_once __DIR__ . '/../database/ConexionMediciones.php';

// --------------------
// dispositivo_id: Text, tipo: Text, valor: R --> guardarMediciones() -->
// Valida la medición y la inserta usando una consulta preparada.
// --------------------

function guardarMediciones(string $dispositivoId, string $tipo, float $valor, string $lugar, ?PDO $conexion = null): void {
    if ($dispositivoId === '') {
        throw new InvalidArgumentException('dispositivo_id no puede estar vacío');
    }
    if (!in_array($tipo, ['CO2', 'TEMPERATURA'], true)) {
        throw new InvalidArgumentException('tipo debe ser CO2 o TEMPERATURA');
    }
    if (!is_finite($valor)) {
        throw new InvalidArgumentException('valor debe ser un número finito');
    }
    if ($lugar === '') {
        throw new InvalidArgumentException('lugar no puede estar vacío');
    }
    $conexion = $conexion ?? conectarBaseDatos(obtenerEntornoBaseDatos());
    $parametros = ['dispositivo_id' => $dispositivoId, 'tipo' => $tipo, 'valor' => $valor, 'lugar' => $lugar];
    $consultasInsert = [
        'INSERT INTO mediciones (dispositivo_id, tipo, valor, lugar) VALUES (:dispositivo_id, :tipo, :valor, :lugar)',
        'INSERT INTO Mediciones (dispositivo_id, Tipo, Valor, Lugar) VALUES (:dispositivo_id, :tipo, :valor, :lugar)',
    ];
    ejecutarPrimeraConsultaValida($conexion, $consultasInsert, $parametros);
}

// --------------------
// dispositivo_id: Text, fecha: DateTime, tipo: Text, valor: R ] <-- mostrarMediciones() <--
// Lee y normaliza las filas para devolver el contrato compartido.
// --------------------

function mostrarMediciones(?PDO $conexion = null): array {
    $conexion = $conexion ?? conectarBaseDatos(obtenerEntornoBaseDatos());
    $consultasSelect = [
        "SELECT dispositivo_id AS dispositivo_id, tipo AS tipo, valor AS valor, lugar AS lugar,
                DATE_FORMAT(fecha, '%Y-%m-%dT%H:%i:%s.%f') AS fecha
         FROM mediciones ORDER BY fecha DESC, dispositivo_id DESC",
        "SELECT dispositivo_id AS dispositivo_id, Tipo AS tipo, Valor AS valor, Lugar AS lugar,
                DATE_FORMAT(Fecha, '%Y-%m-%dT%H:%i:%s.%f') AS fecha
         FROM Mediciones ORDER BY Fecha DESC, dispositivo_id DESC",
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
// fila: Dict --> normalizarFilaMedicion() --> (dispositivo_id: Text, fecha: DateTime, tipo: Text, valor: R)
// Normaliza tipos y nombres de columna antes de entregar una medición.
// --------------------

function normalizarFilaMedicion(array $fila): array {
    return [
        'dispositivo_id' => (string) ($fila['dispositivo_id'] ?? $fila['Dispositivo_Id'] ?? $fila['Dispositivo_id'] ?? ''),
        'tipo' => (string) ($fila['tipo'] ?? $fila['Tipo'] ?? ''),
        'valor' => (float) ($fila['valor'] ?? $fila['Valor'] ?? 0),
        'lugar' => (string) ($fila['lugar'] ?? $fila['Lugar'] ?? 'Ubicación no disponible'),
        'fecha' => (string) ($fila['fecha'] ?? $fila['Fecha'] ?? ''),
    ];
}