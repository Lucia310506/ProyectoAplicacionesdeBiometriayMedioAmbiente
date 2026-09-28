/*
 * Fichero: ConfiguracionRest.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Centraliza la dirección del servidor REST de mediciones.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

public final class ConfiguracionRest {
    // Dirección pública HTTPS del servidor desplegado en Plesk.
    public static final String URL_MEDICIONES =
            "https://ldiamur.upv.edu.es/rest/mediciones.php";

    // --> ConfiguracionRest() -->
    private ConfiguracionRest() { }
}
