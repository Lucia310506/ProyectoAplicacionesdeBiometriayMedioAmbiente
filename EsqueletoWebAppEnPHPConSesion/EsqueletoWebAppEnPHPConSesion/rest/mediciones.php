<?php
/*
 * Fichero: mediciones.php
 * Autor: Lucia
 * Descripción: Ruta REST para crear y consultar mediciones ambientales.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucia
 */

require_once __DIR__ . '/../logica/mediciones.php';

header('Content-Type: application/json; charset=utf-8');

// peticion: Text --> responder_mediciones() -->
function responder_mediciones(): void {
    try {
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $cuerpo = json_decode(file_get_contents('php://input'), true);
            if (!is_array($cuerpo) || !array_key_exists('tipo', $cuerpo)
                || !array_key_exists('valor', $cuerpo) || !is_numeric($cuerpo['valor'])) {
                http_response_code(400);
                echo json_encode(['error' => 'Se requieren tipo y valor numérico']);
                return;
            }

            guardar_mediciones((string) $cuerpo['tipo'], (float) $cuerpo['valor']);
            http_response_code(201);
            echo json_encode(['resultado' => 'medición guardada']);
            return;
        }

        if ($_SERVER['REQUEST_METHOD'] === 'GET') {
            echo json_encode(mostrar_mediciones());
            return;
        }

        http_response_code(405);
        header('Allow: GET, POST');
        echo json_encode(['error' => 'Método no permitido']);
    } catch (InvalidArgumentException $error) {
        http_response_code(422);
        echo json_encode(['error' => $error->getMessage()]);
    } catch (Throwable $error) {
        error_log($error->getMessage());
        http_response_code(500);
        echo json_encode(['error' => 'Error interno del servidor']);
    }
}

responder_mediciones();
