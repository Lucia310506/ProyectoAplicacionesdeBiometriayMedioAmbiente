/*
 * Fichero: MainActivity.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Clase base de la app, que escanea los beacon, que pide permisos y filtra los beacons.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;
// ------------------------------------------------------------------
// ------------------------------------------------------------------

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.UUID;
import java.util.Collections;
import org.json.JSONArray;

// ------------------------------------------------------------------
// ------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;
    private boolean resultadoTestLogicaFake = false;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE( resultado );
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        this.elEscanner.startScan( this.callbackDelEscaneo);

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void mostrarInformacionDispositivoBTLE( ScanResult resultado ) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        /*
        ParcelUuid[] puuids = bluetoothDevice.getUuids();
        if ( puuids.length >= 1 ) {
            //Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].getUuid());
           // Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].toString());
        }*/

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi );

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado ) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");


        // super.onScanResult(ScanSettings.SCAN_MODE_LOW_LATENCY, result); para ahorro de energía

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanResult() ");

                mostrarInformacionDispositivoBTLE( resultado );
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");

            }
        };

        ScanFilter sf = new ScanFilter.Builder().setDeviceName( dispositivoBuscado ).build();

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado );
        //Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado
          //      + " -> " + Utilidades.stringToUUID( dispositivoBuscado ) );

        // A diferencia del escaneo general, aquí sí se aplica el filtro creado arriba.
        this.elEscanner.startScan(
                Collections.singletonList(sf),
                new ScanSettings.Builder().build(),
                this.callbackDelEscaneo);
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {

        if ( this.callbackDelEscaneo == null ) {
            return;
        }

        this.elEscanner.stopScan( this.callbackDelEscaneo );
        this.callbackDelEscaneo = null;

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado" );
        iniciarServicioEscucha();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );
        iniciarServicioEscucha();

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado" );
        stopService(new Intent(this, ServicioEscuharBeacons.class));
    } // ()

    private void iniciarServicioEscucha() {
        Intent intent = new Intent(this, ServicioEscuharBeacons.class);
        intent.setAction(ServicioEscuharBeacons.ACCION_INICIAR);
        ContextCompat.startForegroundService(this, intent);
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
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

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");

        }
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
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
     * --> comprobarLogicaFakeAlArrancar() -->
     * --------------------
     */
    private void comprobarLogicaFakeAlArrancar() {
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
            Log.i("TEST_LOGICAFake", "OK: pruebas de logica fake superadas al iniciar");
        } catch (Throwable error) {
            Log.e("TEST_LOGICAFake", "ERROR: fallaron las pruebas de logica fake al iniciar", error);
        }
    }

    /*
     * --------------------
     * tipo: Text, valor: R, nombre: Text --> comprobarRechazoLogica() -->
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
     * --> comprobarRestAlArrancar() -->
     * --------------------
     */
    private void comprobarRestAlArrancar() {
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
                        Log.e("TEST_PETICIONARIO_REST", "ERROR: fallo la comprobacion REST al iniciar", error);
                    }
                    Log.i("TESTS_APP", "RESULTADOS : LOGICA_FAKE="
                            + (resultadoTestLogicaFake ? "OK" : "ERROR")
                            + ", REST=" + (resultadoRest ? "OK" : "ERROR"));
                    pedirPermisosNecesarios();
                });
    }
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        Log.i("TESTS_APP", "EJECUTAR TESTS");

        comprobarLogicaFakeAlArrancar();
        comprobarRestAlArrancar();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");

    } // onCreate()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
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
// --------------------------------------------------------------
// --------------------------------------------------------------
