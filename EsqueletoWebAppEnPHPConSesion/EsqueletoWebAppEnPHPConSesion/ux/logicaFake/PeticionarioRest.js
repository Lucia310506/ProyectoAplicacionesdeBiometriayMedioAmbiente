/*
 * Fichero: PeticionarioRest.js
 * Autor: Lucia
 * Descripción: Cliente REST de la página de mediciones.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucia
 */

const URL_MEDICIONES = '../rest/mediciones.php';

// tipo: Text, valor: R --> enviar_medicion() --x
async function enviar_medicion(tipo, valor) {
  const respuesta = await fetch(URL_MEDICIONES, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ tipo: tipo, valor: Number(valor) })
  });
  const cuerpo = await respuesta.json();
  if (!respuesta.ok) {
    throw new Error(cuerpo.error || 'No se pudo guardar la medición');
  }
  return cuerpo;
}

// mediciones: [ (id: N, tipo: Text, valor: R, fecha: R) ] <-- pedir_mediciones() --x
async function pedir_mediciones() {
  const respuesta = await fetch(URL_MEDICIONES);
  const cuerpo = await respuesta.json();
  if (!respuesta.ok) {
    throw new Error(cuerpo.error || 'No se pudieron consultar las mediciones');
  }
  return cuerpo;
}
