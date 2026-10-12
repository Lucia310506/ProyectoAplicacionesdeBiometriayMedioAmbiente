/*
 * Fichero: Utilidades.java
 * Autor: Lucía Díaz Murcia
 * Descripción: Clase que permite realizar conversiones.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
package com.example.ldiamur.btlealumnos2021app;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

// -----------------------------------------------------------------------------------
// @author: Jordi Bataller Mascarell
// -----------------------------------------------------------------------------------
public class Utilidades {

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * texto: Texto --> stringToBytes() --> [Bytes]
     * Convierte el texto en su representación de bytes.
     * --------------------
     */
    public static byte[] stringToBytes ( String texto ) {
        return texto.getBytes();
        // byte[] b = string.getBytes(StandardCharsets.UTF_8); // Ja
    } // ()

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * uuid: Texto --> stringToUUID() --> UUID
     * Interpreta los 16 caracteres del UUID del proyecto.
     * --------------------
     */
    public static UUID stringToUUID( String uuid ) {
        if ( uuid.length() != 16 ) {
            throw new Error( "stringUUID: string no tiene 16 caracteres ");
        }
        byte[] comoBytes = uuid.getBytes();

        // Se divide en dos mitades para conservar el orden de los 16 bytes al crear el UUID.
        String masSignificativo = uuid.substring(0, 8);
        String menosSignificativo = uuid.substring(8, 16);
        UUID res = new UUID( Utilidades.bytesToLong( masSignificativo.getBytes() ), Utilidades.bytesToLong( menosSignificativo.getBytes() ) );

        // Log.d( MainActivity.ETIQUETA_LOG, " \n\n***** stringToUUID *** " + uuid  + "=?=" + Utilidades.uuidToString( res ) );

        // UUID res = UUID.nameUUIDFromBytes( comoBytes ); no va como quiero

        return res;
    } // ()

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * uuid: UUID --> uuidToString() --> Texto
     * Convierte el UUID en una cadena de sus bytes.
     * --------------------
     */
    public static String uuidToString ( UUID uuid ) {
        return bytesToString( dosLongToBytes( uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() ) );
    } // ()

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * uuid: UUID --> uuidToHexString() --> Texto
     * Representa los bytes del UUID en hexadecimal.
     * --------------------
     */
    public static String uuidToHexString ( UUID uuid ) {
        return bytesToHexString( dosLongToBytes( uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() ) );
    } // ()

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * bytes: [Bytes] --> bytesToString() --> Texto
     * Convierte los bytes en caracteres.
     * --------------------
     */
    public static String bytesToString( byte[] bytes ) {
        if (bytes == null ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append( (char) b );
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * masSignificativos y menosSignificativos: Z --> dosLongToBytes() --> [Bytes]
     * Une dos enteros de 64 bits en una secuencia de 16 bytes.
     * --------------------
     */
    public static byte[] dosLongToBytes( long masSignificativos, long menosSignificativos ) {
        // ByteBuffer usa big-endian por defecto, igual que el orden de los campos de la trama.
        ByteBuffer buffer = ByteBuffer.allocate( 2 * Long.BYTES );
        buffer.putLong( masSignificativos );
        buffer.putLong( menosSignificativos );
        return buffer.array();
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * bytes: [Bytes] --> bytesToInt() --> Z
     * Interpreta los bytes como entero con signo.
     * --------------------
     */
    public static int bytesToInt( byte[] bytes ) {
        // BigInteger conserva el signo en complemento a dos, necesario para temperaturas negativas.
        return new BigInteger(bytes).intValue();
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * bytes: [Bytes] --> bytesToLong() --> Z
     * Interpreta los bytes como entero largo con signo.
     * --------------------
     */
    public static long bytesToLong( byte[] bytes ) {
        return new BigInteger(bytes).longValue();
    }

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * bytes: [Bytes] --> bytesToIntOK() --> Z
     * Convierte hasta cuatro bytes acumulando su valor numérico.
     * --------------------
     */
    public static int bytesToIntOK( byte[] bytes ) {
        if (bytes == null ) {
            return 0;
        }

        if ( bytes.length > 4 ) {
            throw new Error( "demasiados bytes para pasar a int ");
        }
        int res = 0;



        for( byte b : bytes ) {
           /*
           Log.d( MainActivity.ETIQUETA_LOG, "bytesToInt(): byte: hex=" + Integer.toHexString( b )
                   + " dec=" + b + " bin=" + Integer.toBinaryString( b ) +
                   " hex=" + Byte.toString( b )
           );
           */
            res =  (res << 8) // * 16
                    + (b & 0xFF); // para quedarse con 1 byte (2 cuartetos) de lo que haya en b
        } // for

        if ( (bytes[ 0 ] & 0x8) != 0 ) {
            // si tiene signo negativo (un 1 a la izquierda del primer byte
            res = -(~(byte)res)-1; // complemento a 2 (~) de res pero como byte, -1
        }
       /*
        Log.d( MainActivity.ETIQUETA_LOG, "bytesToInt(): res = " + res + " ~res=" + (res ^ 0xffff)
                + "~res=" + ~((byte) res)
        );
        */

        return res;
    } // ()

    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /*
     * --------------------
     * bytes: [Bytes] --> bytesToHexString() --> Texto
     * Formatea cada byte en hexadecimal para depuración.
     * --------------------
     */
    public static String bytesToHexString( byte[] bytes ) {

        if (bytes == null ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
            sb.append(':');
        }
        return sb.toString();
    } // ()
} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------


