// -*-c++-*-

// --------------------------------------------------------------
// Jordi Bataller i Mascarell
// 2019-07-07
// --------------------------------------------------------------
/*
 * Fichero: HolaMundoIBeacon.ino
 * Autor: Lucía Díaz Murcia
 * Descripción: Programa principal que ejecuta la emisión del beacon.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
// https://learn.sparkfun.com/tutorials/nrf52840-development-with-arduino-and-circuitpython

// https://stackoverflow.com/questions/29246805/can-an-ibeacon-have-a-data-payload

// --------------------------------------------------------------
// --------------------------------------------------------------
#include <bluefruit.h>

#undef min // vaya tela, están definidos en bluefruit.h y  !
#undef max // colisionan con los de la biblioteca estándar

// --------------------------------------------------------------
// --------------------------------------------------------------
#include "LED.h"
#include "PuertoSerie.h"

// --------------------------------------------------------------
// --------------------------------------------------------------
namespace Globales {
  
  LED elLED ( /* NUMERO DEL PIN LED = */ 7 );

  PuertoSerie elPuerto ( /* velocidad = */ 115200 ); // 115200 o 9600 o ...

  // Serial1 en el ejemplo de Curro creo que es la conexión placa-sensor 
};

// --------------------------------------------------------------
// --------------------------------------------------------------
#include "EmisoraBLE.h"
#include "Publicador.h"
#include "Medidor.h"
#include "test.h"


// --------------------------------------------------------------
// --------------------------------------------------------------
namespace Globales {

  Publicador elPublicador;

  Medidor elMedidor;

}; // namespace

// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------
// Punto de inicialización de la placa antes de activar BLE.
// --------------------
void inicializarPlaquita () {

  // de momento nada

} // ()

// --------------------------------------------------------------
// setup()
// --------------------------------------------------------------
// --------------------
// Inicializa serie, emisora BLE y medidor al arrancar la placa.
// --------------------
void setup() {

  Globales::elPuerto.esperarDisponible();
  inicializarPlaquita();

  // Suspend Loop() to save power
  Globales::elPublicador.encenderEmisora();
  Globales::elMedidor.iniciarMedidor();
  esperar( 1000 );

  Globales::elPuerto.escribir( "---- setup(): fin ---- \n " );

} // setup ()

// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------
// Ejecuta la secuencia visual de señalización del ciclo.
// --------------------
inline void lucecitas() {
  using namespace Globales;

  elLED.brillar( 100 ); // 100 encendido
  esperar ( 400 ); //  100 apagado
  elLED.brillar( 100 ); // 100 encendido
  esperar ( 400 ); //  100 apagado
  Globales::elLED.brillar( 100 ); // 100 encendido
  esperar ( 400 ); //  100 apagado
  Globales::elLED.brillar( 1000 ); // 1000 encendido
  esperar ( 1000 ); //  100 apagado
} // ()

// --------------------------------------------------------------
// loop ()
// --------------------------------------------------------------
namespace Loop {
  uint8_t cont = 0;
};

// ..............................................................
// ..............................................................
// --------------------
// Lee y publica CO2 y temperatura en cada ciclo del firmware.
// --------------------
void loop () {

  using namespace Loop;
  using namespace Globales;

  // Ejecuta las pruebas solo al enviar t o T desde el monitor serie.
  if (Serial.available() > 0) {
    const char comando = static_cast<char>(Serial.read());
    if (comando == 't' || comando == 'T') {
      ejecutarTestsArduino();
    }
  }
  cont++;

  elPuerto.escribir( "\n---- loop(): empieza " );
  elPuerto.escribir( cont );
  elPuerto.escribir( "\n" );


  lucecitas();
  // mido y publico
  int valorCO2 = elMedidor.medirCO2();
  
  elPublicador.publicarCO2( valorCO2,
							cont,
							2000 // intervalo de emisión
							);
  // mido y publico
  int valorTemperatura = elMedidor.medirTemperatura();
  
  elPublicador.publicarTemperatura( valorTemperatura, 
									cont,
									2000 // intervalo de emisión
									);
  elPuerto.escribir( "---- loop(): acaba **** " );
  elPuerto.escribir( cont );
  elPuerto.escribir( "\n" );
  
} // loop ()
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
