/*
 * Fichero: PeticionarioRESTTest.java
 * Descripción: Comprueba el cliente REST Android contra un servidor HTTP local.
 */
package com.example.ldiamur.btlealumnos2021app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
public class PeticionarioRESTTest {
    private final MockWebServer servidor = new MockWebServer();
    private String urlOriginal;

    /* Inicia el servidor local y dirige temporalmente el cliente hacia él. */
    @Before
    public void preparar() throws Exception {
        servidor.start();
        urlOriginal = ConfiguracionRest.URL_MEDICIONES;
        ConfiguracionRest.URL_MEDICIONES = servidor.url("/mediciones").toString();
    }

    /* Recupera la URL de la aplicación y apaga el servidor tras cada caso. */
    @After
    public void restaurar() throws Exception {
        ConfiguracionRest.URL_MEDICIONES = urlOriginal;
        servidor.shutdown();
    }

    /* Comprueba que GET usa la ruta correcta y entrega el JSON recibido. */
    @Test
    public void getDevuelveLasMedicionesDelServidor() throws Exception {
        servidor.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("[{\"id\":7,\"tipo\":\"CO2\",\"valor\":500,\"fecha\":\"2026-10-09T10:00:00\"}]"));

        Respuesta respuesta = ejecutar("GET", null);
        RecordedRequest peticion = servidor.takeRequest(1, TimeUnit.SECONDS);

        assertNotNull("Debe llegar la petición al servidor local", peticion);
        assertEquals("GET", peticion.getMethod());
        assertEquals("/mediciones", peticion.getPath());
        assertEquals(200, respuesta.codigo);
        assertEquals("[{\"id\":7,\"tipo\":\"CO2\",\"valor\":500,\"fecha\":\"2026-10-09T10:00:00\"}]", respuesta.cuerpo);
    }

    /* Comprueba que POST envía solo tipo y valor y recibe HTTP 201. */
    @Test
    public void postEnviaTipoYValorYRecibe201() throws Exception {
        servidor.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"resultado\":\"guardada\"}"));

        String cuerpo = PeticionarioREST.crearCuerpoMedicion("CO2", 500.0);
        Respuesta respuesta = ejecutar("POST", cuerpo);
        RecordedRequest peticion = servidor.takeRequest(1, TimeUnit.SECONDS);

        assertNotNull("Debe llegar la petición al servidor local", peticion);
        assertEquals("POST", peticion.getMethod());
        assertEquals("/mediciones", peticion.getPath());
        JSONObject jsonEnviado = new JSONObject(peticion.getBody().readUtf8());
        assertEquals(2, jsonEnviado.length());
        assertEquals("CO2", jsonEnviado.getString("tipo"));
        assertEquals(500.0, jsonEnviado.getDouble("valor"), 0.0);
        assertEquals(201, respuesta.codigo);
        assertEquals("{\"resultado\":\"guardada\"}", respuesta.cuerpo);
    }

    /* Ejecuta AsyncTask en UI y espera el callback principal sin bloquearlo. */
    private Respuesta ejecutar(String metodo, String cuerpo) throws Exception {
        CountDownLatch finalizada = new CountDownLatch(1);
        AtomicReference<Respuesta> resultado = new AtomicReference<>();
        AtomicReference<Looper> hiloCallback = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                new PeticionarioREST().hacerPeticionREST(metodo,
                        ConfiguracionRest.URL_MEDICIONES, cuerpo, (codigo, texto) -> {
                            resultado.set(new Respuesta(codigo, texto));
                            hiloCallback.set(Looper.myLooper());
                            finalizada.countDown();
                        }));

        assertTrue("La respuesta debe recibirse", finalizada.await(5, TimeUnit.SECONDS));
        assertEquals("El callback debe ejecutarse en el hilo principal",
                Looper.getMainLooper(), hiloCallback.get());
        return resultado.get();
    }

    private static final class Respuesta {
        private final int codigo;
        private final String cuerpo;

        private Respuesta(int codigo, String cuerpo) {
            this.codigo = codigo;
            this.cuerpo = cuerpo;
        }
    }
}
