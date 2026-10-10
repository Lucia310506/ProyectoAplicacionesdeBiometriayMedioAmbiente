/*
 * Fichero: PeticionarioREST.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Peticionario HTTP del servidor REST de mediciones.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;

public class PeticionarioREST extends AsyncTask<Void, Void, Boolean> {

    interface AbridorConexion {
        HttpURLConnection abrir(URL url) throws IOException;
    }

    public interface RespuestaREST {
        /*
         * --------------------
         * codigo: N, cuerpo: Text --> callback() -->
         * Entrega al cliente el estado HTTP y el cuerpo de respuesta.
         * --------------------
         */
        void callback (int codigo, String cuerpo);
    }

    private String elMetodo;
    private String urlDestino;
    private String elCuerpo = null;
    private RespuestaREST laRespuesta;
    private final AbridorConexion abridorConexion;

    private int codigoRespuesta;
    private String cuerpoRespuesta = "";

    /*
     * --------------------
     * metodo: Texto, URL: Texto, cuerpo: Texto, laRespuesta: RespuestaREST --> hacerPeticionREST() -->
     * Configura y lanza la petición HTTP en segundo plano.
     * --------------------
     */
    public void hacerPeticionREST (String metodo, String urlDestino, String cuerpo, RespuestaREST laRespuesta) {
        this.elMetodo = metodo;
        this.urlDestino = urlDestino;
        this.elCuerpo = cuerpo;
        this.laRespuesta = laRespuesta;

        this.execute();
    }

    /*
     * --------------------
     *  PeticionarioREST() -->
     * Construye el cliente HTTP asíncrono.
     * --------------------
     */
    public PeticionarioREST() {
        this(url -> (HttpURLConnection) url.openConnection());
    }

    /*
     * --------------------
     * abridor: AbridorConexion --> PeticionarioREST() -->
     * Permite sustituir el transporte por uno simulado durante las pruebas.
     * --------------------
     */
    PeticionarioREST(AbridorConexion abridor) {
        this.abridorConexion = abridor;
        Log.d("clienterestandroid", "constructor()");
    }

    /*
     * --------------------
     * tipo: Text, valor: R --> crearCuerpoMedicion() --> JSON
     * Construye el cuerpo del contrato POST sin campos adicionales.
     * --------------------
     */
    static String crearCuerpoMedicion(String tipo, double valor) throws Exception {
        JSONObject cuerpo = new JSONObject();
        cuerpo.put("tipo", tipo);
        cuerpo.put("valor", valor);
        return cuerpo.toString();
    }

    /*
     * --------------------
     * tipo: Text, valor: R --> enviarMedicion()
     * Forma el JSON y envía una medición al endpoint REST.
     * --------------------
     */
    public static void enviarMedicion(String tipo, double valor) {
        try {
            new PeticionarioREST().hacerPeticionREST(
                    "POST", ConfiguracionRest.URL_MEDICIONES, crearCuerpoMedicion(tipo, valor),
                    (codigo, respuesta) -> {
                        if (codigo == 201) {
                            Log.i("clienterestandroid", "OK: POST /mediciones respondió HTTP 201");
                        } else {
                            Log.e("clienterestandroid", "ERROR: POST /mediciones respondió HTTP "
                                    + codigo + " cuerpo=" + respuesta);
                        }
                    });
        } catch (Exception error) {
            Log.e("clienterestandroid", "enviarMedicion(): no se pudo crear el JSON", error);
        }
    }

    @Override
    /*
     * --------------------
     * parámetros: Void[] --> doInBackground() --> B
     * Ejecuta la conexión y lee la respuesta fuera del hilo principal.
     * --------------------
     */
    protected Boolean doInBackground(Void... params) {
        Log.d("clienterestandroid", "doInBackground()");

        try {
            Log.d("clienterestandroid", "doInBackground() me conecto a >" + urlDestino + "<");

            URL url = new URL(urlDestino);
            HttpURLConnection connection = abridorConexion.abrir(url);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod(this.elMetodo);
            connection.setDoInput(true);

            if (!this.elMetodo.equals("GET") && this.elCuerpo != null) {
                Log.d("clienterestandroid", "doInBackground(): no es get, pongo cuerpo");
                byte[] cuerpoUtf8 = this.elCuerpo.getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(cuerpoUtf8.length);
                OutputStream salida = connection.getOutputStream();
                salida.write(cuerpoUtf8);
                salida.flush();
                salida.close();
            }

            Log.d("clienterestandroid", "doInBackground(): petición enviada ");

            int rc = connection.getResponseCode();
            String rm = connection.getResponseMessage();
            String respuesta = "" + rc + " : " + rm;
            Log.d("clienterestandroid", "doInBackground() recibo respuesta = " + respuesta);
            this.codigoRespuesta = rc;

            try {
                InputStream is = rc >= HttpURLConnection.HTTP_BAD_REQUEST
                        ? connection.getErrorStream() : connection.getInputStream();
                if (is == null) {
                    connection.disconnect();
                    return true;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                Log.d("clienterestandroid", "leyendo cuerpo");
                StringBuilder acumulador = new StringBuilder();
                String linea;
                while ((linea = br.readLine()) != null) {
                    Log.d("clienterestandroid", linea);
                    acumulador.append(linea);
                }
                Log.d("clienterestandroid", "FIN leyendo cuerpo");

                this.cuerpoRespuesta = acumulador.toString();
                Log.d("clienterestandroid", "cuerpo recibido=" + this.cuerpoRespuesta);

                connection.disconnect();

            } catch (IOException ex) {
                Log.d("clienterestandroid", "doInBackground(): no se pudo leer el cuerpo", ex);
                connection.disconnect();
            }

            return true;

        } catch (Exception ex) {
            Log.d("clienterestandroid", "doInBackground(): ocurrio alguna otra excepcion: " + ex.getMessage());
        }

        return false;
    }

    @Override
    /*
     * --------------------
     * comoFue: B --> onPostExecute() <--
     * Entrega la respuesta HTTP al callback tras finalizar la petición.
     * --------------------
     */
    protected void onPostExecute(Boolean comoFue) {
        Log.d("clienterestandroid", "onPostExecute() comoFue = " + comoFue);
        if (this.laRespuesta != null) {
            this.laRespuesta.callback(this.codigoRespuesta, this.cuerpoRespuesta);
        }
    }
}
