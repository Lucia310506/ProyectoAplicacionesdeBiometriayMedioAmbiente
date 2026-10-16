// -*- mode: c++ -*-
/*
 * Fichero: EmisoraBLE.h
 * Autor: LucÃƒÆ’Ã‚Â­a DÃƒÆ’Ã‚Â­az Murcia
 * DescripciÃƒÆ’Ã‚Â³n: Controla el funcionamiento de la emisora.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 LucÃƒÆ’Ã‚Â­a DÃƒÆ’Ã‚Â­az Murcia
 * AportaciÃƒÆ’Ã‚Â³n: crear el archivo y hacerlo funcional
 */
// ----------------------------------------------------------
// Jordi Bataller i Mascarell
// 2019-07-07
// ----------------------------------------------------------
#ifndef EMISORA_H_INCLUIDO
#define EMISORA_H_INCLUIDO

// Buena introducciÃƒÆ’Ã‚Â³n: https://learn.adafruit.com/introduction-to-bluetooth-low-energy/gap
// https://os.mbed.com/blog/entry/BLE-Beacons-URIBeacon-AltBeacons-iBeacon/

// fuente: https://www.instructables.com/id/Beaconeddystone-and-Adafruit-NRF52-Advertise-Your-/
// https://github.com/nkolban/ESP32_BLE_Arduino/blob/master/src/BLEBeacon.h

// https://os.mbed.com/blog/entry/BLE-Beacons-URIBeacon-AltBeacons-iBeacon/
// https://learn.adafruit.com/bluefruit-nrf52-feather-learning-guide/bleadvertising

// ----------------------------------------------------------
// ----------------------------------------------------------
#include "ServicioEnEmisora.h"

// ----------------------------------------------------------
// ----------------------------------------------------------
class EmisoraBLE {
private:

  const char * nombreEmisora;
  const uint16_t fabricanteID;
  const int8_t txPower;

public:

  // .........................................................
  // .........................................................
  using CallbackConexionEstablecida = void ( uint16_t connHandle );
  using CallbackConexionTerminada = void ( uint16_t connHandle, uint8_t reason);

  // .........................................................
  // .........................................................
// --------------------
// nombreEmisora_: Texto, fabricante: N, txPower_: Z --> EmisoraBLE() --> EmisoraBLE
// Guarda los parÃƒÆ’Ã‚Â¡metros de emisiÃƒÆ’Ã‚Â³n sin activar el hardware.
// --------------------
  EmisoraBLE( const char * nombreEmisora_, const uint16_t fabricanteID_,
			  const int8_t txPower_ ) 
	:
	nombreEmisora( nombreEmisora_ ) ,
	fabricanteID( fabricanteID_ ) ,
	txPower( txPower_ )
  {
	// no encender ahora la emisora, tal vez sea por el println()
	// que hace que todo falle si lo llamo en el contructor
	// ( = antes que configuremos Serial )
	// No parece que sea por el println,
	// por tanto NO_encenderEmisora();
  } // ()

  // .........................................................
  // .........................................................
  // .........................................................
  // .........................................................
// --------------------
// --> encenderEmisora() 
// Inicializa Bluefruit y detiene cualquier anuncio previo.
// --------------------
  void encenderEmisora() {
	// Serial.println ( "Bluefruit.begin() " );
	 Bluefruit.begin(); 

	 // por si acaso:
	 (*this).detenerAnuncio();
  } // ()

  // .........................................................
  // .........................................................
// --------------------
// cbce:callbacks, cbce:callbacks  --> encenderEmisora() 
// Activa la emisora e instala los callbacks de conexiÃƒÆ’Ã‚Â³n.
// --------------------
  void encenderEmisora( CallbackConexionEstablecida cbce,
						CallbackConexionTerminada cbct ) {

	encenderEmisora();

	// COMENTARIO: registro directo sin funciones wrapper para las conexiones BLE.
	Bluefruit.Periph.setConnectCallback( cbce );
	Bluefruit.Periph.setDisconnectCallback( cbct );

  } // ()

  // .........................................................
  // .........................................................
// --------------------
// --> detenerAnuncio() 
// Detiene la radio solo si está anunciando.
// --------------------
  void detenerAnuncio() {

	if ( (*this).estaAnunciando() ) {
	  // Serial.println ( "Bluefruit.Advertising.stop() " );
	  Bluefruit.Advertising.stop(); 
	}

  }  // ()
  
 
// --------------------
// --> estaAnunciando() --> B
// Consulta el estado de emision de Bluefruit.
// --------------------
  bool estaAnunciando() {
	return Bluefruit.Advertising.isRunning();
  } // ()

  // .........................................................
  // .........................................................
// --------------------
// beaconUUID: N, major: Z, minor: Z, rssi: N --> emitirAnuncioIBeacon() -->
// Configura el paquete iBeacon y comienza a anunciarlo.
// Major contiene tipo/contador; Minor conserva el valor con signo.
// --------------------
  void emitirAnuncioIBeacon( uint8_t * beaconUUID, int16_t major, int16_t minor, uint8_t rssi ) {

	//
	//
	//
	(*this).detenerAnuncio();
	Bluefruit.Advertising.clearData();
	Bluefruit.ScanResponse.clearData();
	Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
	
	//
	// creo el beacon 
	//
	BLEBeacon elBeacon( beaconUUID, major, minor, rssi );
	elBeacon.setManufacturer( (*this).fabricanteID );

	//
	// parece que esto debe ponerse todo aquÃƒÆ’Ã‚Â­
	//

	Bluefruit.setTxPower( (*this).txPower );
	Bluefruit.setName( (*this).nombreEmisora );
	Bluefruit.ScanResponse.addName(); // para que envÃƒÆ’Ã‚Â­e el nombre de emisora (?!)

	//
	// pongo el beacon
	//
	Bluefruit.Advertising.setBeacon( elBeacon );

	//
	// ? quÃƒÆ’Ã‚Â© valorers poner aquÃƒÆ’Ã‚Â­
	//
	Bluefruit.Advertising.restartOnDisconnect(true); // no hace falta, pero lo pongo
	Bluefruit.Advertising.setInterval(100, 100);    // in unit of 0.625 ms

	//
	// empieza el anuncio, 0 = tiempo indefinido (ya lo pararÃƒÆ’Ã‚Â¡n)
	//
	Bluefruit.Advertising.start( 0 ); 
	
  } // ()

  // .........................................................
  //
  // Ejemplo de Beacon (31 bytes)
  //
  // https://os.mbed.com/blog/entry/BLE-Beacons-URIBeacon-AltBeacons-iBeacon/
  //
  // The iBeacon Prefix contains the hex data : 0x0201061AFF004C0215. This breaks down as follows:
  //
  // 0x020106 defines the advertising packet as BLE General Discoverable and BR/EDR high-speed incompatible.
  // Effectively it says this is only broadcasting, not connecting.
  //
  // 0x1AFF says the following data is 26 bytes long and is Manufacturer Specific Data.
  //
  // 0x004C is AppleÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢s Bluetooth Sig ID and is the part of this spec that makes it Apple-dependent.
  //
  // 0x02 is a secondary ID that denotes a proximity beacon, which is used by all iBeacons.
  //
  // 0x15 defines the remaining length to be 21 bytes (16+2+2+1).
  //
  // Por ejemmplo:
  //
  // 1. prefijo: 9bytes
  //       0x02, 0x01, 0x06,       // advFlags 3bytes
  //       0x1a, 0xff,             // advHeader 2 (0x1a = 26 = 25(lenght de 0x4c a 0xca)+1)   0xFF -> BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA
  //       0x4c, 0x00,             // companyID 2bytes
  //       0x02,                   // ibeacon type 1 byte
  //       0x15,                   // ibeacon length 1 byte (dec=21 lo que va a continuaciÃƒÆ’Ã‚Â³n: desde  la 'f' hasta 0x01)
  // 
  // 2. uuid: 16bytes
  // 'f', 'i', 's', 't', 'r', 'o', 'f', 'i', 's', 't', 'r', 'o', 0xa7, 0x10, 0x96, 0xe0
  // 
  // 2 major: 2bytes
  // 0x04, 0xd2,
  // 
  // minor: 2bytes
  // 0x10, 0xe1,
  // 
  // 0xca, // tx power : 1bytes
  //
  // 0x01, // este es el byte 31 = BLE_GAP_ADV_SET_DATA_SIZE_MAX, parece que sobra
  //
  // .........................................................
  // Para enviar como carga libre los últimos 21 bytes de un iBeacon (lo que normalmente serÃƒÆ’Ã‚Â­a uuid-16 major-2 minor-2 txPower-1)
  // .........................................................
  /*
  void emitirAnuncioIBeaconLibre( const char * carga ) {

	const uint8_t tamanyoCarga = strlen( carga );
  */

}; // class

#endif

// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------

