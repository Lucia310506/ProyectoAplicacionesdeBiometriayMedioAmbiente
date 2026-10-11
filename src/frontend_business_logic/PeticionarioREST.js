/*
 * Fichero: PeticionarioREST.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Cliente REST de solo lectura para las mediciones web.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

// Un único punto para XAMPP o Plesk. Relativo a ux/Aplicacion.html.
const URL_MEDICIONES = '/mediciones';

// --------------------
// mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ] <-- pedirMediciones() --x
// Solicita el GET y valida el JSON y el estado HTTP.
// --------------------

async function pedirMediciones() {
  const respuesta = await fetch(URL_MEDICIONES);
  const textoRespuesta = await respuesta.text();
  let mediciones;
  try {
    mediciones = JSON.parse(textoRespuesta);
  } catch (error) {
    throw new Error(
      'El servidor no ha devuelto JSON. Comprueba Plesk, ConfiguracionProduccion.php y la URL ' + URL_MEDICIONES
    );
  }
  if (!respuesta.ok) {
    throw new Error(mediciones.error || 'No se pudieron obtener las mediciones');
  }
  return mediciones;
}

// Exponer globalmente para tests y uso directo en navegador (window) o vm/Node (global)
const globalObj = (typeof window !== 'undefined') ? window : (typeof global !== 'undefined' ? global : this);
globalObj.pedirMediciones = pedirMediciones;
globalObj.URL_MEDICIONES = URL_MEDICIONES;

// Also explicitly on window for browser tests
if (typeof window !== 'undefined') {
  window.pedirMediciones = pedirMediciones;
  window.URL_MEDICIONES = URL_MEDICIONES;
}

// Soporte Node/CommonJS para tests
if (typeof module !== 'undefined') {
  module.exports = { pedirMediciones, URL_MEDICIONES };
}
