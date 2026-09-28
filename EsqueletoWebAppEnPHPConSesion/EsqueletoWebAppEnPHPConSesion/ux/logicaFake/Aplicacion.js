/*
 * Fichero: Aplicacion.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Actualiza el gráfico web con las mediciones recibidas por REST.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

// texto: Text, esError: B --> mostrarEstado() -->
function mostrarEstado(texto, esError) {
  const estado = document.getElementById('estado');
  estado.textContent = texto;
  estado.className = esError ? 'error' : 'ok';
}

// mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ] --> dibujarGrafico() -->
function dibujarGrafico(mediciones) {
  const grafico = document.getElementById('grafico');
  grafico.replaceChildren();
  if (mediciones.length === 0) {
    mostrarEstado('No hay mediciones almacenadas.', false);
    return;
  }
  const grupos = {
    CO2: mediciones.filter(medicion => medicion.tipo === 'CO2'),
    TEMPERATURA: mediciones.filter(medicion => medicion.tipo === 'TEMPERATURA')
  };
  dibujarLinea(grupos.CO2, '#087a66', 0, grafico);
  dibujarLinea(grupos.TEMPERATURA, '#d15b24', 20, grafico);
}

// mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ], color: Text, desplazamiento: R, grafico: Text --> dibujarLinea() -->
function dibujarLinea(mediciones, color, desplazamiento, grafico) {
  if (mediciones.length === 0) return;
  const valores = mediciones.map(medicion => Number(medicion.valor));
  const minimo = Math.min(...valores);
  const rango = Math.max(...valores) - minimo || 1;
  const puntos = mediciones.slice().reverse().map((medicion, indice) =>
    `${70 + indice * (760 / Math.max(mediciones.length - 1, 1))},${330 - desplazamiento - ((Number(medicion.valor) - minimo) / rango) * 220}`
  ).join(' ');
  const linea = document.createElementNS('http://www.w3.org/2000/svg', 'polyline');
  linea.setAttribute('points', puntos);
  linea.setAttribute('fill', 'none');
  linea.setAttribute('stroke', color);
  linea.setAttribute('stroke-width', '3');
  grafico.appendChild(linea);
}

// --> actualizarMediciones() -->
async function actualizarMediciones() {
  try {
    mostrarEstado('Actualizando mediciones...', false);
    const mediciones = await mostrarMediciones();
    dibujarGrafico(mediciones);
    if (mediciones.length > 0) {
      mostrarEstado(`Actualizado: ${mediciones.length} mediciones.`, false);
    }
  } catch (error) {
    mostrarEstado(error.message, true);
  }
}

actualizarMediciones();
setInterval(actualizarMediciones, 5000);
