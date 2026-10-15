<?php
/*
 * Fichero: mediciones.php
 * Autor: Lucía Díaz Murcia
 * Descripción: Implementa las rutas REST GET y POST de mediciones.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 * Aportación: crear el archivo y hacerlo funcional
 */

ini_set('display_errors', '0');
require_once __DIR__ . '/../business_logic/mediciones.php';

// --------------------
// metodo: Text, cuerpo: Text --> atenderMediciones() --> (codigo: N, respuesta: Dict|Nulo)
// Valida el contrato HTTP y delega lectura/escritura a la lógica.
// --------------------

function atenderMediciones(string $metodo, string $cuerpo): array {
    try {
        if ($metodo === 'OPTIONS') {
            return [204, null];
        }
        if ($metodo === 'POST') {
            $datos = json_decode($cuerpo, true);
            if (!is_array($datos) || !isset($datos['dispositivo_id']) || (string) $datos['dispositivo_id'] === ''
                || !isset($datos['tipo']) || !isset($datos['valor']) || !is_numeric($datos['valor'])
                || !isset($datos['lugar']) || (string) $datos['lugar'] === '') {
                return [400, ['error' => 'Se requieren dispositivo_id, tipo, valor numérico y lugar']];
            }
            guardarMediciones((string) $datos['dispositivo_id'], (string) $datos['tipo'], (float) $datos['valor'], (string) $datos['lugar']);
            return [201, ['resultado' => 'medición guardada']];
        }
        if ($metodo === 'GET') {
            return [200, mostrarMediciones()];
        }
        return [405, ['error' => 'Método no permitido']];
    } catch (InvalidArgumentException $error) {
        return [422, ['error' => $error->getMessage()]];
    } catch (Throwable $error) {
        error_log($error->getMessage());
        return [500, ['error' => 'Error interno del servidor']];
    }
}

if (!defined('PRUEBA_REST')) {
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type');
    [$codigo, $respuesta] = atenderMediciones($_SERVER['REQUEST_METHOD'], (string) file_get_contents('php://input'));
    http_response_code($codigo);
    header('Content-Type: application/json; charset=utf-8');
    if ($codigo === 405) {
        header('Allow: GET, POST, OPTIONS');
    }
    if ($respuesta !== null) {
        echo json_encode($respuesta);
    }
}
