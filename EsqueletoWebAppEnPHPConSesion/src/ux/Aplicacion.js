/*
 * Fichero: Aplicacion.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Actualiza la lista web con las mediciones recibidas por REST.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

// texto: Text, clase: Text --> mostrarEstado() -->
function mostrarEstado(texto, clase) {
  const estado = document.getElementById('estado');

  if (!estado) {
    return;
  }

  estado.textContent = texto;
  estado.className = clase;
}

let ultimasMediciones = [];

// mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ] --> dibujarLista() -->
function dibujarLista(mediciones) {
  const cuerpo = document.getElementById('cuerpo-mediciones');

  if (!cuerpo) {
    return;
  }

  // Copiar y ordenar de más antigua a más nueva
  const ordenadas = [...mediciones].sort(
    (a, b) => new Date(a.fecha) - new Date(b.fecha)
  );

  // Obtener las 10 mediciones más recientes
  const ultimas = ordenadas.slice(-10);

  // Borrar las que había
  cuerpo.replaceChildren();

  // Mostrar las 10 más recientes
  ultimas.forEach((medicion) => {
    const fila = document.createElement('tr');

    const celdaTipo = document.createElement('td');
    const celdaValor = document.createElement('td');
    const celdaFecha = document.createElement('td');

    celdaTipo.textContent = String(medicion.tipo);
    celdaValor.textContent = String(medicion.valor);
    celdaFecha.textContent = String(medicion.fecha);

    fila.appendChild(celdaTipo);
    fila.appendChild(celdaValor);
    fila.appendChild(celdaFecha);

    cuerpo.appendChild(fila);
  });
}

// --> actualizarMediciones() -->
async function actualizarMediciones() {
  try {
    mostrarEstado('Cargando mediciones...', 'carga');
    const mediciones = await pedirMediciones();
    dibujarLista(mediciones);
    if (mediciones.length > 0) {
      mostrarEstado(`Actualizado: ${mediciones.length} mediciones.`, 'ok');
    } else {
      mostrarEstado('No hay mediciones almacenadas todavía.', 'ok');
    }
  } catch (error) {
    mostrarEstado(error.message, 'error');
  }
}

// --> iniciarAplicacion() -->
function iniciarAplicacion() {
  actualizarMediciones();
  setInterval(actualizarMediciones, 5000);
}


if (typeof window !== 'undefined' && typeof document !== 'undefined') {
  const pruebasInicio = window.promesasPruebasInicio;
  if (pruebasInicio && typeof pruebasInicio.then === 'function') {
    pruebasInicio.then(iniciarAplicacion).catch((error) => {
      console.log('ERROR: fallo inesperado al ejecutar tests de inicio — ' + error.message);
      iniciarAplicacion();
    });
  } else {
    iniciarAplicacion();
  }
}
