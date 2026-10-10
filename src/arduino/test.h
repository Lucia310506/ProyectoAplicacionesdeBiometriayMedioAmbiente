// -*- mode: c++ -*-
/*
 * Fichero: test.h
 * Autor: Lucía Díaz Murcia
 * Descripción: Pruebas manuales de emisión de un iBeacon de prueba.
 * Fecha: 2026-10-10
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
#ifndef TEST_H_INCLUIDO
#define TEST_H_INCLUIDO

// --------------------
// contador: N --> ejecutarTestsArduino() -->
// Emite un beacon centinela, comprueba el estado BLE y limpia el anuncio.
// --------------------
void ejecutarTestsArduino(uint8_t contador) {
  using namespace Globales;
  const uint16_t major = (Publicador::MedicionesID::PRUEBA << 8) | contador;
  const uint16_t minor = Publicador::VALOR_BEACON_PRUEBA;
  bool todosCorrectos = true;

  elPuerto.escribir("EJECUTAR TESTS ARDUINO\n");
  elPuerto.escribir("Beacon de prueba: tipo=PRUEBA, contador=");
  elPuerto.escribir(contador);
  elPuerto.escribir(", major=");
  elPuerto.escribir(major);
  elPuerto.escribir(", minor=");
  elPuerto.escribir(minor);
  elPuerto.escribir("\n");

  elPublicador.iniciarBeaconPrueba(contador);
  const bool anuncioActivo = elPublicador.estaAnunciando();
  if (anuncioActivo) {
    elPuerto.escribir("OK: anuncio BLE iniciado; beacon disponible para escaneo\n");
    esperar(3000);
  } else {
    elPuerto.escribir("ERROR: no se inició el anuncio BLE de prueba\n");
    todosCorrectos = false;
  }

  elPublicador.detenerBeaconPrueba();
  const bool anuncioDetenido = !elPublicador.estaAnunciando();
  if (anuncioDetenido) {
    elPuerto.escribir("OK: beacon de prueba detenido\n");
  } else {
    elPuerto.escribir("ERROR: el anuncio BLE sigue activo\n");
    todosCorrectos = false;
  }

  elPuerto.escribir("RESULTADOS ARDUINO: ");
  elPuerto.escribir(todosCorrectos ? "OK (2/2)\n" : "ERROR\n");
}

#endif
