/*
 * Fichero: Aplicacion.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Actualiza el gráfico web con las mediciones recibidas por REST.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

/*
 * --------------------
 * texto: Text, es_error: B --> mostrarEstado() -->
 * --------------------
 */
function mostrarEstado(texto, esError) {
  const estado = document.getElementById('estado');
  estado.textContent = texto;
  estado.className = esError ? 'error' : 'ok';
}

/*
 * --------------------
 * mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ] --> dibujarLista() -->
 * --------------------
 */
function dibujarLista(mediciones) {
  const lista = document.getElementById('lista-mediciones');
  if (!lista) {
    return;
  }
  lista.replaceChildren();
  mediciones.forEach((medicion) => {
    const elemento = document.createElement('li');
    elemento.textContent = `${medicion.tipo}: ${medicion.valor} - ${medicion.fecha}`;
    lista.appendChild(elemento);
  });
}



/*
 * --------------------
 * --> actualizarMediciones() -->
 * --------------------
 */
async function actualizarMediciones() {
  try {
    mostrarEstado('Cargando mediciones...', false);
    const mediciones = await pedirMediciones();
    dibujarLista(mediciones);
    if (mediciones.length > 0) {
      mostrarEstado(`Actualizado: ${mediciones.length} mediciones.`, false);
    }
  } catch (error) {
    mostrarEstado(error.message, true);
  }
}

/*
 * --------------------
 * --> iniciarAplicacion() -->
 * --------------------
 */
function iniciarAplicacion() {
  actualizarMediciones();
  setInterval(actualizarMediciones, 5000);
}

if (typeof window !== 'undefined') {
  iniciarAplicacion();
}
