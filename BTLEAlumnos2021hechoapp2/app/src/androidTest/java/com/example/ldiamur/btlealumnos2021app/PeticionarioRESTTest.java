/*
 * Fichero: PeticionarioRESTTest.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Pruebas de integración HTTP del peticionario REST Android.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class PeticionarioRESTTest {

    /*
     * --------------------
     * --> enviarMedicion() -->
     * --------------------
     */
    @Test
    public void enviarMedicionUsaPostRutaYJsonEsperados() throws Exception {
        MockWebServer servidor = new MockWebServer();
        servidor.start();
        String urlAnterior = ConfiguracionRest.URL_MEDICIONES;
        try {
            ConfiguracionRest.URL_MEDICIONES = servidor.url("/mediciones").toString();
            servidor.enqueue(new MockResponse().setResponseCode(201)
                    .setBody("{\"resultado\":\"medición guardada\"}"));

            PeticionarioREST.enviarMedicion("CO2", 500);

            RecordedRequest peticion = servidor.takeRequest(5, TimeUnit.SECONDS);
            assertTrue("Debe llegar una petición", peticion != null);
            assertEquals("POST", peticion.getMethod());
            assertEquals("/mediciones", peticion.getPath());
            JSONObject cuerpo = new JSONObject(peticion.getBody().readUtf8());
            assertEquals(2, cuerpo.length());
            assertEquals("CO2", cuerpo.getString("tipo"));
            assertEquals(500, cuerpo.getDouble("valor"), 0.0);
        } finally {
            ConfiguracionRest.URL_MEDICIONES = urlAnterior;
            servidor.shutdown();
        }
    }

    /*
     * --------------------
     * --> hacerPeticionREST() -->
     * --------------------
     */
    @Test
    public void hacerPeticionRestInformaRespuesta201YErrorHttp() throws Exception {
        MockWebServer servidor = new MockWebServer();
        servidor.start();
        try {
            servidor.enqueue(new MockResponse().setResponseCode(201).setBody("{\"ok\":true}"));
            assertEquals(201, esperarCodigo(servidor.url("/mediciones").toString()));

            servidor.enqueue(new MockResponse().setResponseCode(500).setBody("{\"error\":\"fallo\"}"));
            assertEquals(500, esperarCodigo(servidor.url("/mediciones").toString()));
        } finally {
            servidor.shutdown();
        }
    }

    /*
     * --------------------
     * url: Text --> esperarCodigo() --> N
     * --------------------
     */
    private int esperarCodigo(String url) throws InterruptedException {
        CountDownLatch terminada = new CountDownLatch(1);
        int[] codigo = new int[]{0};
        new PeticionarioREST().hacerPeticionREST("POST", url, "{\"tipo\":\"CO2\",\"valor\":500}",
                (codigoRecibido, cuerpo) -> {
                    codigo[0] = codigoRecibido;
                    terminada.countDown();
                });
        assertTrue("La petición debe terminar", terminada.await(5, TimeUnit.SECONDS));
        return codigo[0];
    }
}
