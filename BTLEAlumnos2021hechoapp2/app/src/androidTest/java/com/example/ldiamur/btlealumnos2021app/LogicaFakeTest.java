/*
 * Fichero: LogicaFakeTest.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Pruebas instrumentadas de la lógica fake con resultado visible en LogCat.
 * Fecha: 2026-10-03
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

import static org.junit.Assert.fail;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LogicaFakeTest {
    private static final String ETIQUETA = "TEST_LOGICAFake";

    /*
     * --------------------
     * guardarMedicionesAceptaTiposPermitidos()
     * --------------------
     */
    @Test
    public void guardarMedicionesAceptaTiposPermitidos() {
        LogicaFake.guardarMediciones("CO2", 500.0);
        LogicaFake.guardarMediciones("TEMPERATURA", -19.0);
        Log.i(ETIQUETA, "OK: guardarMediciones acepta CO2 y TEMPERATURA");
    }

    /*
     * --------------------
     * guardarMedicionesRechazaTipoDesconocido()
     * --------------------
     */
    @Test
    public void guardarMedicionesRechazaTipoDesconocido() {
        comprobarRechazo("HUMEDAD", 50.0);
        Log.i(ETIQUETA, "OK: guardarMediciones rechaza tipos desconocidos");
    }

    /*
     * --------------------
     * guardarMedicionesRechazaValorNoFinito()
     * --------------------
     */
    @Test
    public void guardarMedicionesRechazaValorNoFinito() {
        comprobarRechazo("CO2", Double.NaN);
        comprobarRechazo("CO2", Double.POSITIVE_INFINITY);
        Log.i(ETIQUETA, "OK: guardarMediciones rechaza valores no finitos");
    }

    /*
     * --------------------
     * tipo: Text, valor: R --> comprobarRechazo()
     * --------------------
     */
    private void comprobarRechazo(String tipo, double valor) {
        try {
            LogicaFake.guardarMediciones(tipo, valor);
            fail("La medición inválida debía rechazarse");
        } catch (IllegalArgumentException esperado) {
            // El rechazo esperado completa la aserción.
        }
    }
}
