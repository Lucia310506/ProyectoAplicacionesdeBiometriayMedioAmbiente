/*
 * Fichero: PeticionarioREST.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Peticionario HTTP del servidor REST de mediciones.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;

public class PeticionarioREST extends AsyncTask<Void, Void, Boolean> {

    /*
     * --------------------
     * codigo: N, cuerpo: Text --> callback() -->
     * --------------------
     */
    public interface RespuestaREST {
        void callback (int codigo, String cuerpo);
    }

    private String elMetodo;
    private String urlDestino;
    private String elCuerpo = null;
    private RespuestaREST laRespuesta;

    private int codigoRespuesta;
    private String cuerpoRespuesta = "";

    /*
     * --------------------
     * metodo: Text, url: Text, cuerpo: Text --> hacerPeticionREST() -->
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
     * --> PeticionarioREST() -->
     * --------------------
     */
    public PeticionarioREST() {
        Log.d("clienterestandroid", "constructor()");
    }

    /*
     * --------------------
     * tipo: Text, valor: R --> enviarMedicion() --x
     * --------------------
     */
    public static void enviarMedicion(String tipo, double valor) {
        try {
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("tipo", tipo);
            cuerpo.put("valor", valor);
            new PeticionarioREST().hacerPeticionREST(
                    "POST", ConfiguracionRest.URL_MEDICIONES, cuerpo.toString(),
                    (codigo, respuesta) -> Log.d("clienterestandroid",
                            "enviarMedicion(): código=" + codigo + " cuerpo=" + respuesta));
        } catch (Exception error) {
            Log.e("clienterestandroid", "enviarMedicion(): no se pudo crear el JSON", error);
        }
    }

    /*
     * --------------------
     * --> doInBackground() --> B
     * --------------------
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d("clienterestandroid", "doInBackground()");

        try {
            Log.d("clienterestandroid", "doInBackground() me conecto a >" + urlDestino + "<");

            URL url = new URL(urlDestino);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestMethod(this.elMetodo);
            connection.setDoInput(true);

            if (!this.elMetodo.equals("GET") && this.elCuerpo != null) {
                Log.d("clienterestandroid", "doInBackground(): no es get, pongo cuerpo");
                connection.setDoOutput(true);
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                dos.writeBytes(this.elCuerpo);
                dos.flush();
                dos.close();
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

    /*
     * --------------------
     * resultado: B --> onPostExecute() -->
     * --------------------
     */
    @Override
    protected void onPostExecute(Boolean comoFue) {
        Log.d("clienterestandroid", "onPostExecute() comoFue = " + comoFue);
        if (this.laRespuesta != null) {
            this.laRespuesta.callback(this.codigoRespuesta, this.cuerpoRespuesta);
        }
    }
}
