/*
 * Fichero: TramaIBeacon.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Permite obtener los datos de los beacon.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

import java.util.Arrays;

// -----------------------------------------------------------------------------------
// @author: Jordi Bataller i Mascarell
// -----------------------------------------------------------------------------------
public class TramaIBeacon {
    private byte[] prefijo = null; // 9 bytes
    private byte[] uuid = null; // 16 bytes
    private byte[] major = null; // 2 bytes
    private byte[] minor = null; // 2 bytes
    private byte txPower = 0; // 1 byte

    private byte[] losBytes;

    private byte[] advFlags = null; // 3 bytes
    private byte[] advHeader = null; // 2 bytes
    private byte[] companyID = new byte[2]; // 2 bytes
    private byte iBeaconType = 0 ; // 1 byte
    private byte iBeaconLength = 0 ; // 1 byte

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getPrefijo() --> Bytes
     * Devuelve los bytes iniciales del anuncio.
     * --------------------
     */
    public byte[] getPrefijo() {
        return prefijo;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getUUID() --> Bytes
     * Devuelve el UUID contenido en el beacon.
     * --------------------
     */
    public byte[] getUUID() {
        return uuid;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getMajor() --> Bytes
     * Devuelve los bytes Major del beacon.
     * --------------------
     */
    public byte[] getMajor() {
        return major;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getMinor() --> Bytes
     * Devuelve los bytes Minor del beacon.
     * --------------------
     */
    public byte[] getMinor() {
        return minor;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getTxPower() --> Z
     * Devuelve la potencia transmitida en la trama.
     * --------------------
     */
    public byte getTxPower() {
        return txPower;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getLosBytes() --> Bytes
     * Devuelve la trama BLE completa recibida.
     * --------------------
     */
    public byte[] getLosBytes() {
        return losBytes;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getAdvFlags() --> Bytes
     * Devuelve las banderas del anuncio BLE.
     * --------------------
     */
    public byte[] getAdvFlags() {
        return advFlags;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getAdvHeader() --> Bytes
     * Devuelve la cabecera de datos de anuncio.
     * --------------------
     */
    public byte[] getAdvHeader() {
        return advHeader;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getCompanyID() --> Bytes
     * Devuelve el identificador del fabricante.
     * --------------------
     */
    public byte[] getCompanyID() {
        return companyID;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getiBeaconType() --> Z
     * Devuelve el tipo de anuncio iBeacon.
     * --------------------
     */
    public byte getiBeaconType() {
        return iBeaconType;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * --> getiBeaconLength() --> Z
     * Devuelve la longitud declarada de datos iBeacon.
     * --------------------
     */
    public byte getiBeaconLength() {
        return iBeaconLength;
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * bytes: Bytes --> TramaIBeacon() --> TramaIBeacon
     * Separa la trama recibida en sus campos iBeacon.
     * --------------------
     */
    public TramaIBeacon(byte[] bytes ) {
        this.losBytes = bytes;

        prefijo = Arrays.copyOfRange(losBytes, 0, 8+1 ); // 9 bytes
        uuid = Arrays.copyOfRange(losBytes, 9, 24+1 ); // 16 bytes
        major = Arrays.copyOfRange(losBytes, 25, 26+1 ); // 2 bytes
        minor = Arrays.copyOfRange(losBytes, 27, 28+1 ); // 2 bytes
        txPower = losBytes[ 29 ]; // 1 byte

        advFlags = Arrays.copyOfRange( prefijo, 0, 2+1 ); // 3 bytes
        advHeader = Arrays.copyOfRange( prefijo, 3, 4+1 ); // 2 bytes
        companyID = Arrays.copyOfRange( prefijo, 5, 6+1 ); // 2 bytes
        iBeaconType = prefijo[ 7 ]; // 1 byte
        iBeaconLength = prefijo[ 8 ]; // 1 byte

    } // ()
} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------


