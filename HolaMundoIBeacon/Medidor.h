// -*- mode: c++ -*-
/*
 * Fichero: Medidor.h
 * Autor: Lucía Díaz Murcia
 * Descripción: Simula las mediciones.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
#ifndef MEDIDOR_H_INCLUIDO
#define MEDIDOR_H_INCLUIDO

// ------------------------------------------------------
// ------------------------------------------------------
class Medidor {

  // .....................................................
  // .....................................................
private:

public:

  // .....................................................
  // constructor
  // .....................................................
  // --------------------
// --> Medidor()
// Construye el simulador sin recursos externos.
// --------------------

  // Construye el simulador sin recursos externos.
  Medidor(  ) {
  } // ()

  // .....................................................
  // .....................................................
  // --------------------
// --> iniciarMedidor() -->
// Reserva la inicialización para sensores físicos.
// --------------------

  // Reserva la inicialización para sensores físicos.
  void iniciarMedidor() {
	// las cosas que no se puedan hacer en el constructor, if any
  } // ()

  // .....................................................
  // .....................................................
  // --------------------
// --> medirCO2() --> Z
// Devuelve la lectura simulada de CO2.
// --------------------

  // Devuelve la lectura simulada de CO2.
  int medirCO2() {
	return 18;
  } // ()

  // .....................................................
  // .....................................................
  // --------------------
// --> medirTemperatura() --> Z
// Devuelve la lectura simulada de temperatura.
// --------------------

  // Devuelve la lectura simulada de temperatura.
  int medirTemperatura() {
	return 8; // qué frío !
  } // ()
	
}; // class

// ------------------------------------------------------
// ------------------------------------------------------
// ------------------------------------------------------
// ------------------------------------------------------
#endif
