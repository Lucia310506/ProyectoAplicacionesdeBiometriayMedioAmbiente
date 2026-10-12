/*
 * Fichero: LogicaFake.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Fachada de dominio para las consultas que necesita la interfaz web.
 * Fecha: 2026-10-09
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

/*
 * --------------------
 * Entradas: ninguna.
 * Tipo: Medicion = (id: N, tipo: Text, valor: R, fecha: DateTime).
 * --> mostrarMediciones() --> Mediciones = [Medicion]
 * Responsabilidad: obtiene mediciones por REST y las entrega a la UX.
 * Salida: lista Mediciones; propaga errores REST o si la respuesta no es una lista.
 * --------------------
 */
async function mostrarMediciones() {
  // Usa pedirMediciones expuesta globalmente por PeticionarioREST.js (window/global)
  const globalObj = (typeof window !== 'undefined') ? window : (typeof global !== 'undefined' ? global : this);
  const pedirMediciones = globalObj.pedirMediciones;
  const mediciones = await pedirMediciones();
  if (!Array.isArray(mediciones)) {
    throw new Error('La lógica fake esperaba una lista de mediciones');
  }
  return mediciones;
}

// Exponer globalmente para tests y uso directo en navegador (window) o vm/Node (global)
const globalObj2 = (typeof window !== 'undefined') ? window : (typeof global !== 'undefined' ? global : this);
globalObj2.mostrarMediciones = mostrarMediciones;

// Also explicitly on window for browser tests
if (typeof window !== 'undefined') {
  window.mostrarMediciones = mostrarMediciones;
}

// Soporte Node/CommonJS para tests
if (typeof module !== 'undefined') {
  module.exports = { mostrarMediciones };
}
