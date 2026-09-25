/*
 * Fichero: ConfiguracionRest.java
 * Autor: Lucia
 * Descripción: Centraliza la dirección del servidor REST de mediciones.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucia
 */
package com.example.ldiamur.btlealumnos2021app;

public final class ConfiguracionRest {
    // Cambiar esta IP por la IPv4 del ordenador con XAMPP en la misma red Wi-Fi.
    public static final String URL_MEDICIONES =
            "http://192.168.1.41/Sprint0/rest/mediciones.php";

    private ConfiguracionRest() { }
}
