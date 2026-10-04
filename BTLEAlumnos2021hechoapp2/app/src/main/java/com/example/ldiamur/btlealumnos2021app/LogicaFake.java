/*
 * Fichero: LogicaFake.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Lógica fake que procesa las mediciones recibidas del beacon.
 * Fecha: 2026-10-03
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

import android.util.Log;

public class LogicaFake {

    /*
     * --------------------
     * tipo: Text, valor: R --> guardarMediciones() -->
     * Valida el tipo y el valor antes de aceptar la medición simulada.
     * --------------------
     */
    public static void guardarMediciones(String tipo, double valor) {
        if (!"CO2".equals(tipo) && !"TEMPERATURA".equals(tipo)) {
            throw new IllegalArgumentException("Tipo de medición no válido: " + tipo);
        }
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new IllegalArgumentException("Valor de medición no válido: " + valor);
        }
        Log.d("logicafakeandroid", "Medición aceptada: " + tipo + "=" + valor);
    }
}
