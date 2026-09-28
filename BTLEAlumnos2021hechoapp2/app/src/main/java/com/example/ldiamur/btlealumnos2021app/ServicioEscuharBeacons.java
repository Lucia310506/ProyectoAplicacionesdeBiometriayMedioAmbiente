/*
 * Fichero: ServicioEscuharBeacons.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Servicio en segundo plano que escucha beacons y publica mediciones.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.Collections;
import java.util.List;

// Escucha los anuncios BLE aunque la actividad ya no esté visible.
public class ServicioEscuharBeacons extends Service {

    public static final String ACCION_INICIAR =
            "com.example.ldiamur.btlealumnos2021app.INICIAR_ESCANEO";
    private static final String ETIQUETA_LOG = ">>>>";
    private static final String NOMBRE_BEACON = "GTI-3A";
    private static final String CANAL_ID = "escucha_beacons";
    private static final int ID_NOTIFICACION = 1001;

    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo;
    private int ultimoTipoMedida = -1;
    private int ultimoContador = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        crearCanalNotificacion();
        iniciarPrimerPlano();
        prepararEscaner();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        iniciarEscaneo();
        return START_STICKY;
    }

    private void prepararEscaner() {
        BluetoothAdapter adaptador = BluetoothAdapter.getDefaultAdapter();
        if (adaptador == null) {
            Log.e(ETIQUETA_LOG, "El dispositivo no dispone de Bluetooth BLE");
            stopSelf();
            return;
        }
        elEscanner = adaptador.getBluetoothLeScanner();
    }

    private boolean tengoPermisoEscaneo() {
        String permiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? Manifest.permission.BLUETOOTH_SCAN
                : Manifest.permission.ACCESS_FINE_LOCATION;
        return ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED;
    }

    private void iniciarEscaneo() {
        if (elEscanner == null || !tengoPermisoEscaneo()) {
            Log.e(ETIQUETA_LOG, "No se puede iniciar el escaneo: falta Bluetooth o permiso");
            return;
        }
        if (callbackDelEscaneo != null) {
            return;
        }

        callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                procesarBeacon(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> resultados) {
                super.onBatchScanResults(resultados);
                for (ScanResult resultado : resultados) {
                    procesarBeacon(resultado);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e(ETIQUETA_LOG, "Error al escanear beacons: " + errorCode);
            }
        };

        ScanFilter filtro = new ScanFilter.Builder()
                .setDeviceName(NOMBRE_BEACON)
                .build();
        ScanSettings ajustes = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        elEscanner.startScan(Collections.singletonList(filtro), ajustes, callbackDelEscaneo);
        Log.d(ETIQUETA_LOG, "Servicio: escuchando beacons " + NOMBRE_BEACON);
    }

    private void procesarBeacon(ScanResult resultado) {
        if (resultado.getScanRecord() == null || resultado.getScanRecord().getBytes().length < 30) {
            return;
        }

        BluetoothDevice dispositivoBluetooth = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();
        TramaIBeacon trama = new TramaIBeacon(bytes);

        // Se conserva el registro detallado original para poder inspeccionar
        // cada beacon desde Logcat, aunque ahora lo genere el servicio.
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre = " + dispositivoBluetooth.getName());
        Log.d(ETIQUETA_LOG, " toString = " + dispositivoBluetooth);
        Log.d(ETIQUETA_LOG, " dirección = " + dispositivoBluetooth.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi);
        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = "
                + Utilidades.bytesToHexString(bytes));
        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(trama.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(trama.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(trama.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(trama.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = "
                + Integer.toHexString(trama.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = "
                + Integer.toHexString(trama.getiBeaconLength()) + " ( "
                + trama.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(trama.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(trama.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(trama.getMajor()) + "( "
                + Utilidades.bytesToInt(trama.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(trama.getMinor()) + "( "
                + Utilidades.bytesToInt(trama.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(trama.getTxPower())
                + " ( " + trama.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

        int major = Utilidades.bytesToInt(trama.getMajor());
        int tipoMedida = major >> 8;
        int contador = major & 0xff;
        int valor = Utilidades.bytesToInt(trama.getMinor());

        if (tipoMedida == 11) {
            Log.d(ETIQUETA_LOG, "Servicio: CO2 = " + valor + " ppm (muestra " + contador + ")");
            enviarMedicionNueva("CO2", valor, tipoMedida, contador);
        } else if (tipoMedida == 12) {
            Log.d(ETIQUETA_LOG, "Servicio: temperatura = " + valor + " °C (muestra " + contador + ")");
            enviarMedicionNueva("TEMPERATURA", valor, tipoMedida, contador);
        }
    }

    // tipo: Text, valor: R, tipo_medida: N, contador: N --> enviarMedicionNueva() -->
    private void enviarMedicionNueva(String tipo, int valor, int tipoMedida, int contador) {
        if (ultimoTipoMedida == tipoMedida && ultimoContador == contador) {
            return;
        }
        ultimoTipoMedida = tipoMedida;
        ultimoContador = contador;
        PeticionarioREST.guardarMedicion(tipo, valor);
    }

    private void detenerEscaneo() {
        if (elEscanner != null && callbackDelEscaneo != null) {
            elEscanner.stopScan(callbackDelEscaneo);
            callbackDelEscaneo = null;
        }
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID, "Escucha de beacons", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(canal);
        }
    }

    private void iniciarPrimerPlano() {
        Notification.Builder creador = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CANAL_ID)
                : new Notification.Builder(this);
        Notification notificacion = creador
                .setContentTitle("Escuchando mediciones BLE")
                .setContentText("Buscando el beacon GTI-3A")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(ID_NOTIFICACION, notificacion);
    }

    @Override
    public void onDestroy() {
        detenerEscaneo();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
