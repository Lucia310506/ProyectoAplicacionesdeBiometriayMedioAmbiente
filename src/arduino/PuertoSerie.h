
// -*- mode: c++ -*-
/*
 * Fichero: PuertoSerie.h
 * Autor: Lucía Díaz Murcia
 * Descripción: Controla el funcionamiento del puerto Serie.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
// ----------------------------------------------------------
// Jordi Bataller i Mascarell
// 2019-07-07
// ----------------------------------------------------------

#ifndef PUERTO_SERIE_H_INCLUIDO
#define PUERTO_SERIE_H_INCLUIDO

// ----------------------------------------------------------
// ----------------------------------------------------------
class PuertoSerie  {

public:
  // .........................................................
  // .........................................................
// --------------------
// baudios: N --> PuertoSerie() --> 
// Abre el puerto serie a la velocidad indicada.
// --------------------
  PuertoSerie (long baudios) {
	Serial.begin( baudios );
	// mejor no poner esto aquí: while ( !Serial ) delay(10);   
  } // ()

  // .........................................................
  // .........................................................
// --------------------
// esperarDisponible() 
// Espera a que el monitor serie esté conectado.
// --------------------
  void esperarDisponible() {

	while ( !Serial ) {
	  delay(10);   
	}

  } // ()

  // .........................................................
  // .........................................................
  template<typename T> //Cualquier tipo
// --------------------
// mensaje: T --> escribir() -->
// Escribe el mensaje en el puerto serie.
// --------------------
  void escribir (T mensaje) {
	Serial.print( mensaje );
  } // ()
  
}; // class PuertoSerie

// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
#endif
