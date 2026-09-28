/*
 * Fichero: PeticionarioREST.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Cliente REST de solo lectura para las mediciones web.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

// La ruta relativa conserva la misma procedencia HTTPS de Aplicacion.html.
const URL_MEDICIONES = '../rest/mediciones.php';

// mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ] <-- pedirMediciones() --x
async function pedirMediciones() {
  const respuesta = await fetch(URL_MEDICIONES);
  const textoRespuesta = await respuesta.text();
  let mediciones;
  try {
    mediciones = JSON.parse(textoRespuesta);
  } catch (error) {
    throw new Error(
      'El servidor no ha devuelto JSON. Comprueba que la URL sea ../rest/mediciones.php, no ../logica/mediciones.php.'
    );
  }
  if (!respuesta.ok) {
    throw new Error(mediciones.error || 'No se pudieron obtener las mediciones');
  }
  return mediciones;
}
