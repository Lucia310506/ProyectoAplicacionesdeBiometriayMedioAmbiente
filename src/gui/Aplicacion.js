/*
 * Fichero: Aplicacion.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Actualiza la lista web con las mediciones recibidas por REST.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

// --------------------
// texto: Text, clase: Text --> mostrarEstado() -->
// Actualiza el mensaje visible y su estilo.
// --------------------

function mostrarEstado(texto, clase) {
  const estado = document.getElementById('estado');

  if (!estado) {
    return;
  }

  estado.textContent = texto;
  estado.className = clase;
}

// --------------------
// mediciones: Mediciones --> dibujarLista() -->
// Ordena y representa las últimas diez filas sin interpretar HTML externo.
// --------------------

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

// --------------------
// --> actualizarMediciones() -->
// Solicita la lista a la lógica fake, representa los datos y refleja estados.
// --------------------

async function actualizarMediciones() {
  try {
    mostrarEstado('Cargando mediciones...', 'carga');
    const mediciones = await mostrarMediciones();
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

// --------------------
// --> iniciarAplicacion() -->
// Hace la primera actualización y programa la consulta periódica.
// --------------------

function iniciarAplicacion() {
  actualizarMediciones();
  setInterval(actualizarMediciones, 5000);
}


iniciarAplicacion();
