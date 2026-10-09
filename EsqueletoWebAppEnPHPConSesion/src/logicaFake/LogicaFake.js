/*
 * Fichero: LogicaFake.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Fachada de dominio para las consultas que necesita la interfaz web.
 * Fecha: 2026-10-09
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

/*
 * --------------------
 * --> mostrarMediciones() --> [Medicion]
 * Devuelve mediciones de dominio sin exponer fetch ni HTTP a la UX.
 * --------------------
 */
async function mostrarMediciones() {
  const mediciones = await pedirMediciones();
  if (!Array.isArray(mediciones)) {
    throw new Error('La lógica fake esperaba una lista de mediciones');
  }
  return mediciones;
}

if (typeof module !== 'undefined') {
  module.exports = { mostrarMediciones };
}
