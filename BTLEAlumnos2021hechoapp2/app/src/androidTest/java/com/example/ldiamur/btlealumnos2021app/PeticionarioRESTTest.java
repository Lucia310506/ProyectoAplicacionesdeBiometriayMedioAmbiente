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

import android.util.Log;

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
    private static final String ETIQUETA = "TEST_PETICIONARIO_REST";

    /*
     * --------------------
     * --> mostrarMediciones() <--
     * Comprueba que Android consulta GET /mediciones y recibe la lista JSON.
     * --------------------
     */
    @Test
    public void mostrarMedicionesUsaGetYDevuelveLaLista() throws Exception {
        MockWebServer servidor = new MockWebServer();
        servidor.start();
        String urlAnterior = ConfiguracionRest.URL_MEDICIONES;
        CountDownLatch terminada = new CountDownLatch(1);
        int[] codigo = new int[]{0};
        String[] cuerpo = new String[]{""};
        try {
            ConfiguracionRest.URL_MEDICIONES = servidor.url("/mediciones").toString();
            servidor.enqueue(new MockResponse().setResponseCode(200)
                    .setBody("[{\"id\":1,\"tipo\":\"CO2\",\"valor\":500,\"fecha\":\"2026-09-25T10:30:00\"}]"));

            LogicaFake.mostrarMediciones((codigoRecibido, cuerpoRecibido) -> {
                codigo[0] = codigoRecibido;
                cuerpo[0] = cuerpoRecibido;
                terminada.countDown();
            });

            RecordedRequest peticion = servidor.takeRequest(5, TimeUnit.SECONDS);
            assertTrue("Debe llegar una petición", peticion != null);
            assertEquals("GET", peticion.getMethod());
            assertEquals("/mediciones", peticion.getPath());
            assertTrue("La respuesta debe terminar", terminada.await(5, TimeUnit.SECONDS));
            assertEquals(200, codigo[0]);
            assertEquals("CO2", new org.json.JSONArray(cuerpo[0]).getJSONObject(0).getString("tipo"));
            Log.i(ETIQUETA, "OK: GET /mediciones devuelve la lista JSON");
        } finally {
            ConfiguracionRest.URL_MEDICIONES = urlAnterior;
            servidor.shutdown();
        }
    }

    /*
     * --------------------
     * --> enviarMedicion() -->
     * Verifica método POST, ruta y cuerpo JSON.
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
            Log.i(ETIQUETA, "OK: POST /mediciones con JSON de tipo y valor");
        } finally {
            ConfiguracionRest.URL_MEDICIONES = urlAnterior;
            servidor.shutdown();
        }
    }

    /*
     * --------------------
     * --> hacerPeticionREST() -->
     * Comprueba el procesamiento de respuestas HTTP 201 y 500.
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
            Log.i(ETIQUETA, "OK: respuestas HTTP 201 y 500 procesadas");
        } finally {
            servidor.shutdown();
        }
    }

    /*
     * --------------------
     * url: Text --> esperarCodigo() --> N
     * Espera de forma acotada y devuelve el código recibido.
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
