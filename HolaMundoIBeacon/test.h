/*
 * Fichero: test.h
 * Autor: Lucía Díaz Murcia
 * Descripción: Autocomprobaciones de la lógica simulada Arduino al arrancar.
 * Fecha: 2026-10-04
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
#ifndef TEST_H_INCLUIDO
#define TEST_H_INCLUIDO

#include "Medidor.h"

// --------------------
// --> ejecutarTestsArduino() --> B
// Comprueba las mediciones simuladas e informa el resultado por el puerto serie.
// --------------------
inline bool ejecutarTestsArduino() {
  Medidor medidorDePrueba;
  const bool co2Correcto = medidorDePrueba.medirCO2() == 18;
  const bool temperaturaCorrecta = medidorDePrueba.medirTemperatura() == 8;

  Serial.println("EJECUTAR TESTS ARDUINO");
  Serial.println(co2Correcto
      ? "OK: medición simulada de CO2"
      : "ERROR: medición simulada de CO2");
  Serial.println(temperaturaCorrecta
      ? "OK: medición simulada de temperatura"
      : "ERROR: medición simulada de temperatura");

  const bool todosCorrectos = co2Correcto && temperaturaCorrecta;
  Serial.println(todosCorrectos
      ? "RESULTADOS ARDUINO: OK"
      : "RESULTADOS ARDUINO: ERROR");
  return todosCorrectos;
}

#endif