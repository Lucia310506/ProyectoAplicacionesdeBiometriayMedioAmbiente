/*
 * Fichero: LogicaFake.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Lógica fake Android que procesa las mediciones del beacon.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

import android.util.Log;

public class LogicaFake {

    /*
     * --------------------
     * tipo: Text, valor: R --> guardarMediciones() -->
     * --------------------
     */
    public static void guardarMediciones(String tipo, double valor) {
        if (!"CO2".equals(tipo) && !"TEMPERATURA".equals(tipo)) {
            Log.e("logicafakeandroid", "Tipo de medición no válido: " + tipo);
            return;
        }
        PeticionarioREST.enviarMedicion(tipo, valor);
    }
}
