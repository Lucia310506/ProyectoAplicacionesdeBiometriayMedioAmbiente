/*
 * Fichero: PruebasRESTBoton.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Pruebas REST ejecutables desde el botón de la aplicación.
 * Fecha: 2026-10-09
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class PruebasRESTBoton {
    private static final String ETIQUETA = "TEST_PETICIONARIO_REST";

    public interface ResultadoFinal {
        void callback(boolean correcto, String resumen);
    }

    private interface Caso {
        void ejecutar() throws Exception;
    }

    /*
     * --------------------
     * resultado: ResultadoFinal --> ejecutar() -->
     * Ejecuta GET, POST y error HTTP con conexiones simuladas, sin tocar el servidor real.
     * --------------------
     */
    public static void ejecutar(ResultadoFinal resultado) {
        Log.i(ETIQUETA, "EJECUTAR TESTS REST");
        new Bateria(resultado).probarGet();
    }

    /*
     * --------------------
     * nombre: Text, prueba: Prueba --> comprobar() -->
     * Registra el caso y conserva la ejecución de los siguientes si falla.
     * --------------------
     */
    private static void comprobar(String nombre, Caso prueba, Bateria bateria) {
        try {
            prueba.ejecutar();
            bateria.correctos++;
            Log.i(ETIQUETA, "OK: " + nombre);
        } catch (Throwable error) {
            bateria.errores++;
            Log.e(ETIQUETA, "ERROR: " + nombre + " — " + error.getMessage(), error);
        }
    }

    private static final class Bateria {
        private final ResultadoFinal resultado;
        private int correctos;
        private int errores;

        private Bateria(ResultadoFinal resultado) {
            this.resultado = resultado;
        }

        /*
         * --------------------
         * --> probarGet() -->
         * Verifica GET /mediciones y la lista JSON que entrega el servidor simulado.
         * --------------------
         */
        private void probarGet() {
            ConexionFalsa[] conexion = new ConexionFalsa[1];
            conexion[0] = new ConexionFalsa(URL_MEDICIONES(), 200,
                    "[{\"id\":1,\"tipo\":\"CO2\",\"valor\":500,\"fecha\":\"2026-09-25T10:30:00\"}]");
            new PeticionarioREST(url -> conexion[0]).hacerPeticionREST(
                    "GET", ConfiguracionRest.URL_MEDICIONES, null, (codigo, cuerpo) -> {
                        comprobar("GET usa la ruta /mediciones y devuelve JSON", () -> {
                            exigir(codigo == 200, "GET debe responder 200");
                            exigir("/mediciones".equals(conexion[0].getURL().getPath()), "Ruta GET incorrecta");
                            exigir("GET".equals(conexion[0].getRequestMethod()), "Método GET incorrecto");
                            JSONArray mediciones = new JSONArray(cuerpo);
                            exigir(mediciones.length() == 1
                                    && "CO2".equals(mediciones.getJSONObject(0).getString("tipo")),
                                    "JSON GET incorrecto");
                        }, this);
                        probarPost();
                    });
        }

        /*
         * --------------------
         * --> probarPost() -->
         * Comprueba ruta, método, JSON y aceptación de HTTP 201.
         * --------------------
         */
        private void probarPost() {
            ConexionFalsa[] conexion = new ConexionFalsa[1];
            conexion[0] = new ConexionFalsa(URL_MEDICIONES(), 201, "{\"resultado\":\"guardada\"}");
            String cuerpo;
            try {
                cuerpo = PeticionarioREST.crearCuerpoMedicion("CO2", 500.0);
            } catch (Exception error) {
                comprobar("POST crea el JSON esperado", () -> { throw new IllegalStateException(error); }, this);
                probarErrorHttp();
                return;
            }
            new PeticionarioREST(url -> conexion[0]).hacerPeticionREST(
                    "POST", ConfiguracionRest.URL_MEDICIONES, cuerpo, (codigo, respuesta) -> {
                        comprobar("POST /mediciones envía solo tipo y valor y acepta 201", () -> {
                            exigir(codigo == 201, "POST debe responder 201");
                            exigir("/mediciones".equals(conexion[0].getURL().getPath()), "Ruta POST incorrecta");
                            exigir("POST".equals(conexion[0].getRequestMethod()), "Método POST incorrecto");
                            JSONObject json = new JSONObject(new String(
                                    conexion[0].cuerpoEnviado.toByteArray(), StandardCharsets.UTF_8));
                            exigir(json.length() == 2 && "CO2".equals(json.getString("tipo"))
                                    && json.getDouble("valor") == 500.0, "El JSON debe incluir tipo y valor");
                        }, this);
                        probarErrorHttp();
                    });
        }

        /*
         * --------------------
         * --> probarErrorHttp() -->
         * Comprueba que el cliente informa una respuesta HTTP de error.
         * --------------------
         */
        private void probarErrorHttp() {
            ConexionFalsa[] conexion = new ConexionFalsa[1];
            conexion[0] = new ConexionFalsa(URL_MEDICIONES(), 500, "{\"error\":\"fallo simulado\"}");
            new PeticionarioREST(url -> conexion[0]).hacerPeticionREST(
                    "GET", ConfiguracionRest.URL_MEDICIONES, null, (codigo, cuerpo) -> {
                        comprobar("El cliente informa HTTP 500", () ->
                                exigir(codigo == 500 && cuerpo.contains("fallo simulado"),
                                        "El cliente no comunicó el error HTTP"), this);
                        terminar();
                    });
        }

        /*
         * --------------------
         * --> terminar() -->
         * Publica el resultado de la batería y devuelve el control a la pantalla.
         * --------------------
         */
        private void terminar() {
            String resumen = "REST=" + (errores == 0 ? "OK" : "ERROR")
                    + " (" + correctos + "/" + (correctos + errores) + ")";
            Log.i(ETIQUETA, "RESULTADOS: " + resumen);
            resultado.callback(errores == 0, resumen);
        }
    }

    /*
     * --------------------
     * condicion: B, mensaje: Text --> exigir() -->
     * Detiene el caso si no se cumple una comprobación.
     * --------------------
     */
    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new IllegalStateException(mensaje);
    }

    /*
     * --------------------
     * --> URL_MEDICIONES() --> URL
     * Obtiene una URL válida para el servidor HTTP simulado.
     * --------------------
     */
    private static URL URL_MEDICIONES() {
        try {
            return new URL(ConfiguracionRest.URL_MEDICIONES);
        } catch (Exception error) {
            throw new IllegalStateException("URL REST no válida", error);
        }
    }

    private static final class ConexionFalsa extends HttpURLConnection {
        private final int codigo;
        private final byte[] respuesta;
        private final ByteArrayOutputStream cuerpoEnviado = new ByteArrayOutputStream();

        private ConexionFalsa(URL url, int codigo, String respuesta) {
            super(url);
            this.codigo = codigo;
            this.respuesta = respuesta.getBytes(StandardCharsets.UTF_8);
        }

        @Override public void disconnect() { }
        @Override public boolean usingProxy() { return false; }
        @Override public void connect() { connected = true; }
        @Override public OutputStream getOutputStream() { return cuerpoEnviado; }
        @Override public int getResponseCode() { return codigo; }
        @Override public InputStream getInputStream() throws IOException {
            if (codigo >= HTTP_BAD_REQUEST) throw new IOException("HTTP " + codigo);
            return new ByteArrayInputStream(respuesta);
        }
        @Override public InputStream getErrorStream() {
            return codigo >= HTTP_BAD_REQUEST ? new ByteArrayInputStream(respuesta) : null;
        }
    }

    private PruebasRESTBoton() { }
}
