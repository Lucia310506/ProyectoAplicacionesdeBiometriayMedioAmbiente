/*
 * Fichero: MainActivity.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Clase principal que ejecuta las comprobaciones y gestiona los permisos de Bluetooth.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;
// ------------------------------------------------------------------
// ------------------------------------------------------------------

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;

// ------------------------------------------------------------------
// ------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;
    private boolean resultadoTestLogicaFake = false;
    private boolean pruebasEnCurso = false;
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*
     * --------------------
     * v: vista --> botonBuscarDispositivosBTLEPulsado() <--
     * Inicia el servicio persistente de escucha BLE desde la interfaz.
     * --------------------
     */
    public void botonBuscarDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado" );
        iniciarServicioEscucha();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*
     * --------------------
     * v: vista --> botonBuscarNuestroDispositivoBTLEPulsado() <--
     * Inicia la escucha del beacon configurado.
     * --------------------
     */
    public void botonBuscarNuestroDispositivoBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );
        iniciarServicioEscucha();

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*
     * --------------------
     * v: vista --> botonDetenerBusquedaDispositivosBTLEPulsado() <--
     * Detiene el servicio que escucha beacons.
     * --------------------
     */
    public void botonDetenerBusquedaDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado" );
        stopService(new Intent(this, ServicioEscuharBeacons.class));
    } // ()

    /*
     * --------------------
     * --> iniciarServicioEscucha() -->
     * Arranca el servicio Android en primer plano para mantener la escucha.
     * --------------------
     */
    private void iniciarServicioEscucha() {
        Intent intent = new Intent(this, ServicioEscuharBeacons.class);
        intent.setAction(ServicioEscuharBeacons.ACCION_INICIAR);
        ContextCompat.startForegroundService(this, intent);
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*
     * --------------------
     *  inicializarBlueTooth() -->
     * Habilita Bluetooth cuando ya se han concedido los permisos.
     * --------------------
     */
    private void inicializarBlueTooth() {
        // Sin permisos, las llamadas Bluetooth lanzan SecurityException (crash)
        if ( !tengoLosPermisosNecesarios() ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): NO tengo permisos, no inicializo !!!!");
            return;
        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        if ( bta == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hay adaptador BT en este dispositivo !!!!");
            return;
        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");

        bta.enable();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState() );
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*
     * --------------------
     *  permisosNecesarios() --> Texto[]
     * Elige los permisos Bluetooth/localización según la versión Android.
     * --------------------
     */
    /// --------------------------PERMISOS---------------------------------///
    private String[] permisosNecesarios() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.POST_NOTIFICATIONS
                };
            }
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }

        // Hasta Android 11 (API 30) solo hace falta la localización en tiempo de ejecución
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*
     * --------------------
     *  tengoLosPermisosNecesarios() --> B
     * Comprueba que todos los permisos requeridos estén concedidos.
     * --------------------
     */
    private boolean tengoLosPermisosNecesarios() {
        for (String permiso : this.permisosNecesarios()) {
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                Log.d(ETIQUETA_LOG, " tengoLosPermisosNecesarios(): falta el permiso = " + permiso);
                return false;
            }
        }

        return true;
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*
     * --------------------
     * --> pedirPermisosNecesarios() -->
     * Solicita permisos pendientes o inicializa Bluetooth si ya están concedidos.
     * --------------------
     */
    private void pedirPermisosNecesarios() {
        Log.d(ETIQUETA_LOG, " pedirPermisosNecesarios(): comprobando permisos ");

        if ( !this.tengoLosPermisosNecesarios() ) {
            String[] permisos = this.permisosNecesarios();

            Log.d(ETIQUETA_LOG, " pedirPermisosNecesarios(): solicitamos los permisos ");

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    permisos,
                    CODIGO_PETICION_PERMISOS);
        }
        else {
            Log.d(ETIQUETA_LOG, " pedirPermisosNecesarios(): parece que YA tengo los permisos necesarios !!!!");
            this.inicializarBlueTooth();
        }
    } // ()


    /*
     * --------------------
     *  comprobarLogicaFake() -->
     * Comprueba la validación de mediciones válidas e inválidas.
     * --------------------
     */
    private void comprobarLogicaFake() {
        resultadoTestLogicaFake = false;
        try {
            Log.i("TESTS_APP", "Ejecutando test: guardar CO2 valido");
            LogicaFake.guardarMediciones("CO2", 500.0);
            Log.i("TESTS_APP", "OK: guardar CO2 valido");

            Log.i("TESTS_APP", "Ejecutando test: guardar TEMPERATURA valida");
            LogicaFake.guardarMediciones("TEMPERATURA", -19.0);
            Log.i("TESTS_APP", "OK: guardar TEMPERATURA valida");

            comprobarRechazoLogica("HUMEDAD", 50.0, "rechazar tipo no admitido");
            comprobarRechazoLogica("CO2", Double.NaN, "rechazar valor NaN");
            resultadoTestLogicaFake = true;
            Log.i("TEST_LOGICAFake", "OK: pruebas de logica fake superadas");
        } catch (Throwable error) {
            Log.e("TEST_LOGICAFake", "ERROR: fallaron las pruebas de lógica fake", error);
        }
    }

    /*
     * --------------------
     * tipo: Texto, valor: R, nombre: Texto --> comprobarRechazoLogica() -->
     * Confirma que la lógica rechaza la medición inválida.
     * --------------------
     */
    private void comprobarRechazoLogica(String tipo, double valor, String nombre) {
        Log.i("TESTS_APP", "Ejecutando test: " + nombre);
        try {
            LogicaFake.guardarMediciones(tipo, valor);
            throw new AssertionError("La medicion invalida debia rechazarse");
        } catch (IllegalArgumentException esperado) {
            Log.i("TESTS_APP", "OK: " + nombre);
        }
    }

    /*
     * --------------------
     * --> botonEjecutarTestsPulsado() <--
     * Ejecuta las pruebas cuando el usuario pulsa el botón.
     * --------------------
     */
    public void botonEjecutarTestsPulsado(View vista) {
        if (pruebasEnCurso) return;
        pruebasEnCurso = true;
        vista.setEnabled(false);
        TextView estado = findViewById(R.id.estadoTests);
        estado.setText("Ejecutando pruebas...");
        Log.i("TESTS_APP", "EJECUTAR TESTS");
        comprobarLogicaFake();
        comprobarRest();
    }
    /*
     * --------------------
     * --> comprobarRest() -->
     * Comprueba en segundo plano que REST devuelve JSON válido y muestra el resumen.
     * --------------------
     */
    private void comprobarRest() {
        Log.i("TESTS_APP", "Ejecutando test REST: GET /mediciones, HTTP 200 y JSON array");
        new PeticionarioREST().hacerPeticionREST(
                "GET", ConfiguracionRest.URL_MEDICIONES, null,
                (codigo, cuerpo) -> {
                    boolean resultadoRest = false;
                    try {
                        if (codigo != 200) {
                            throw new IllegalStateException("GET /mediciones devolvio HTTP " + codigo);
                        }
                        new JSONArray(cuerpo);
                        resultadoRest = true;
                        Log.i("TEST_PETICIONARIO_REST", "OK: GET /mediciones devolvio HTTP 200 y JSON valido");
                    } catch (Exception error) {
                        Log.e("TEST_PETICIONARIO_REST", "ERROR: falló la comprobación REST", error);
                    }
                    Log.i("TESTS_APP", "RESULTADOS : LOGICA_FAKE="
                            + (resultadoTestLogicaFake ? "OK" : "ERROR")
                            + ", REST=" + (resultadoRest ? "OK" : "ERROR"));
                    TextView estado = findViewById(R.id.estadoTests);
                    estado.setText(resultadoRest && resultadoTestLogicaFake
                            ? "Pruebas Android: OK" : "Pruebas Android: ERROR; revisa Logcat");
                    Button boton = findViewById(R.id.botonEjecutarTests);
                    boton.setEnabled(true);
                    pruebasEnCurso = false;
                });
    }
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    /*
     * --------------------
     * savedInstanceState: Bundle --> onCreate()
     * Configura la pantalla y solicita los permisos Bluetooth.
     * --------------------
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");
        pedirPermisosNecesarios();
        Log.d(ETIQUETA_LOG, " onCreate(): termina ");
    } // onCreate()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    /*
     * --------------------
     * código: N, permisos: Texto[], resultados: Z[] --> onRequestPermissionsResult()
     * Continúa la inicialización si el usuario concede los permisos.
     * --------------------
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // Si la petición se cancela, los arrays vienen vacíos.
                if (grantResults.length == 0 || grantResults.length != permissions.length) {
                    Log.d(ETIQUETA_LOG, " onRequestPermissionsResult(): petición cancelada o fallida !!!!");
                    return;
                }

                boolean todoConcedido = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(ETIQUETA_LOG, " onRequestPermissionsResult(): permiso concedido = " + permissions[i]);
                    } else {
                        Log.d(ETIQUETA_LOG, " onRequestPermissionsResult(): permiso DENEGADO = " + permissions[i]);
                        todoConcedido = false;
                    }
                }

if (todoConcedido) {
                    Log.d(ETIQUETA_LOG, " onRequestPermissionsResult(): TODOS los permisos concedidos !!!!");
                    this.inicializarBlueTooth();
                } else {
                    Log.d(ETIQUETA_LOG, " onRequestPermissionsResult(): Socorro: faltan permisos por conceder !!!!");
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    } // ()

} // class
    // --------------------------------------------------------------
    // --------------------------------------------------------------
