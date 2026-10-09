/*
 * Fichero: test.h
 * Autor: Lucía Díaz Murcia
 * Descripción: Pruebas manuales de las mediciones simuladas.
 * Fecha: 2026-10-09
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
#ifndef TEST_H_INCLUIDO
#define TEST_H_INCLUIDO

#include "Medidor.h"

// --> ejecutarTestsArduino() --> B
// Comprueba los valores simulados cuando se solicita desde el monitor serie.
inline bool ejecutarTestsArduino() {
  Medidor medidorDePrueba;
  const bool co2Correcto = medidorDePrueba.medirCO2() == 18;
  const bool temperaturaCorrecta = medidorDePrueba.medirTemperatura() == 8;
  const bool todoCorrecto = co2Correcto && temperaturaCorrecta;

  Serial.println("EJECUTAR TESTS ARDUINO");
  Serial.println(co2Correcto ? "OK: medición simulada de CO2" : "ERROR: medición simulada de CO2");
  Serial.println(temperaturaCorrecta ? "OK: medición simulada de temperatura" : "ERROR: medición simulada de temperatura");
  Serial.println(todoCorrecto ? "RESULTADOS ARDUINO: OK" : "RESULTADOS ARDUINO: ERROR");
  return todoCorrecto;
}

#endif