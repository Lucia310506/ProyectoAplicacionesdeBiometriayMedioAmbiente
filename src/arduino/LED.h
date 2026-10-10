// -*- mode: c++ -*-
/*
 * Fichero: LED.h
 * Autor: Lucía Díaz Murcia
 * Descripción: Controla el funcionamiento de la LED.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
#ifndef LED_H_INCLUIDO
#define LED_H_INCLUIDO

// ----------------------------------------------------------
// Jordi Bataller i Mascarell
// 2019-07-07
// ----------------------------------------------------------

// ----------------------------------------------------------
// ----------------------------------------------------------
// --------------------
// tiempo: N --> esperar() -->
// Espera el intervalo solicitado.
// --------------------
void esperar (long tiempo) {
  delay (tiempo);
}

// ----------------------------------------------------------
// ----------------------------------------------------------
class LED {
private:
  int numeroLED;
  bool encendido;
public:

  // .........................................................
  // .........................................................
// --------------------
// numero: N --> LED() --> LED
// Configura el pin y deja la luz apagada.
// --------------------
  LED (int numero)
	: numeroLED (numero), encendido(false)
  {
	pinMode(numeroLED, OUTPUT);
	apagar ();
  }

  // .........................................................
  // .........................................................
// --------------------
// --> encender() -->
// Enciende el pin y actualiza el estado local.
// --------------------
  void encender () {
	digitalWrite(numeroLED, HIGH); 
	encendido = true;
  }

  // .........................................................
  // .........................................................
// --------------------
// --> apagar() -->
// Apaga el pin y actualiza el estado local.
// --------------------
  void apagar () {
	  digitalWrite(numeroLED, LOW);
	  encendido = false;
  }

  // .........................................................
  // .........................................................
// --------------------
// --> alternar() -->
// Invierte el estado actual de la luz.
// --------------------
  void alternar () {
	if (encendido) {
	  apagar();
	} else {
	  encender ();
	}
  } // ()

  // .........................................................
  // .........................................................
// --------------------
// tiempo: N --> brillar() -->
// Enciende la luz durante el intervalo y luego la apaga.
// --------------------
  void brillar (long tiempo) {
	encender ();
	esperar(tiempo); 
	apagar ();
  }
}; // class

// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
#endif
