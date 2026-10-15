// -*- mode: c++ -*-
/*
 * Fichero: Publicador.h
 * Autor: Lucía Díaz Murcia
 * Descripción: Esta clase publica los datos de temperatura y CO2 en un beacon.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 * Aportación: crear el archivo y hacerlo funcional
 */
// --------------------------------------------------------------
// Jordi Bataller i Mascarell
// --------------------------------------------------------------

#ifndef PUBLICADOR_H_INCLUIDO
#define PUBLICADOR_H_INCLUIDO

// --------------------------------------------------------------
// --------------------------------------------------------------
class Publicador {

  // ............................................................
  // ............................................................
private:

  // UUID del beacon: identifica el grupo o familia del emisor (16 bytes).
  uint8_t beaconUUID[16] = { 
	'E', 'P', 'S', 'G', '-', 'G', 'T', 'I', 
	'-', 'P', 'R', 'O', 'Y', '-', '3', 'A'
	};

  // ............................................................
  // ............................................................
public:
  // COMENTARIO: el nombre de esta emisora actúa como identificador del sensor si no hay MAC disponible.
  // El sufijo "-01" debe cambiarse en cada placa para que cada sensor sea único.
  EmisoraBLE laEmisora {
	"LuciaGTI-01", //  nombre emisora + sensor ID
	  0x004c, // fabricanteID (Apple)
	  4 // txPower
	  };
  
  const int RSSI = -53; // por poner algo, de momento no lo uso

  // ............................................................
  // ............................................................
public:

  // ............................................................
  // ............................................................
  enum MedicionesID  {
	CO2 = 11,
	TEMPERATURA = 12,
	RUIDO = 13,
	PRUEBA = 14
  };

  // Identificadores de las sondas físicas instaladas en una placa.
  // Solo se reserva un nibble (15 sensores), ampliable cuando se necesite más espacio.
  enum SensorID {
	SENSOR_01 = 1,
	SENSOR_02 = 2,
	SENSOR_03 = 3
  };

  static constexpr int16_t VALOR_BEACON_PRUEBA = 0x5A3C; //Variable Beacon de pruebas.

  // ............................................................
  // ............................................................
  // --------------------
// Publicador()
// Prepara el beacon sin activar la radio durante la construcción.
// --------------------

  Publicador( ) {
	// ATENCION: no hacerlo aquí. (*this).laEmisora.encenderEmisora();
	// Pondremos un método para llamarlo desde el setup() más tarde
  } // ()

  // ............................................................
  // ............................................................
  // --------------------
// --> encenderEmisora() 
// Activa la emisora BLE una vez inicializada la placa.
// --------------------
  void encenderEmisora() {
	(*this).laEmisora.encenderEmisora();
  } // ()

  // ............................................................
  // ............................................................
  // --------------------
// valorCO2: N, contador: N, tiempoEspera: Z --> publicarCO2() -->
// Emite CO2 y detiene el anuncio al terminar el intervalo.
// --------------------
  void publicarCO2( int16_t valorCO2, uint8_t contador,
					long tiempoEspera, uint8_t idSensor = SensorID::SENSOR_01 ) {

	// 1. empezamos anuncio
	// Encoding del mayor: sensor en nibble 15-12, tipo CO2 en nibble 11-8 y contador en bits 7-0.
	uint16_t major = (idSensor << 12) | (MedicionesID::CO2 << 8) | (contador & 0xff);
	(*this).laEmisora.emitirAnuncioIBeacon( (*this).beaconUUID, 
											major,
											valorCO2, // Minor: valor medido (CO2) con signo compactado.
											(*this).RSSI // rssi
									);

	/*
	Globales::elPuerto.escribir( "   publicarCO2(): valor=" );
	Globales::elPuerto.escribir( valorCO2 );
	Globales::elPuerto.escribir( "   contador=" );
	Globales::elPuerto.escribir( contador );
	Globales::elPuerto.escribir( "   todo="  );
	Globales::elPuerto.escribir( major );
	Globales::elPuerto.escribir( "\n" );
	*/

	// 2. esperamos el tiempo que nos digan
	esperar( tiempoEspera );

	// 3. paramos anuncio
	(*this).laEmisora.detenerAnuncio();
  } // ()

  // ............................................................
  // ............................................................
  // --------------------
// valorTemperatura:Z , contador: N, espera: N --> publicarTemperatura() -->
// Emite temperatura y detiene el anuncio al terminar el intervalo.
// --------------------

  void publicarTemperatura( int16_t valorTemperatura,
							uint8_t contador, long tiempoEspera, uint8_t idSensor = SensorID::SENSOR_01 ) {

	// Major: mismo esquema para el tipo TEMPERATURA.
	uint16_t major = (idSensor << 12) | (MedicionesID::TEMPERATURA << 8) | (contador & 0xff);
	(*this).laEmisora.emitirAnuncioIBeacon( (*this).beaconUUID, 
											major,
											valorTemperatura, // Minor: valor medido (temperatura) con signo.
											(*this).RSSI // rssi
									);
	esperar( tiempoEspera );

	(*this).laEmisora.detenerAnuncio();
  } // ()

  // --------------------
  // contador: N --> iniciarBeaconPrueba() -->
  // Anuncia un iBeacon con tipo PRUEBA y un valor centinela reconocible.
  // --------------------
  void iniciarBeaconPrueba( uint8_t contador ) {
	const uint16_t major = (SensorID::SENSOR_01 << 12) | (MedicionesID::PRUEBA << 8) | (contador & 0xff);
	(*this).laEmisora.emitirAnuncioIBeacon(
	  (*this).beaconUUID,
	  major,
	  VALOR_BEACON_PRUEBA,
	  (*this).RSSI
	);
  }

  // --------------------
  // --> estaAnunciando() --> B
  // Devuelve el estado de emisión BLE para comprobar el beacon de prueba.
  // --------------------
  bool estaAnunciando() {
	return (*this).laEmisora.estaAnunciando();
  }

  // --------------------
  // --> detenerBeaconPrueba() -->
  // Detiene el anuncio BLE usado por la batería de pruebas.
  // --------------------
  void detenerBeaconPrueba() {
	(*this).laEmisora.detenerAnuncio();
  }
	
}; // class

// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
#endif
