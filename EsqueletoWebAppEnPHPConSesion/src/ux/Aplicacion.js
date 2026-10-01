/*
 * Fichero: Aplicacion.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Actualiza el gráfico web con las mediciones recibidas por REST.
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

// mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ] --> dibujarLista() -->
function dibujarLista(mediciones) {
  const cuerpo = document.getElementById('cuerpo-mediciones');
  if (!cuerpo) {
    return;
  }
  cuerpo.replaceChildren();
  mediciones.forEach((medicion) => {
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

// mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ] --> dibujarGrafico() -->
function dibujarGrafico(mediciones) {
  const lienzo = document.getElementById('grafico');
  if (!lienzo || typeof lienzo.getContext !== 'function') {
    return;
  }
  const ctx = lienzo.getContext('2d');
  if (!ctx) {
    return;
  }
  const ancho = lienzo.width;
  const alto = lienzo.height;
  ctx.clearRect(0, 0, ancho, alto);
  ctx.fillStyle = '#f8fbfa';
  ctx.fillRect(0, 0, ancho, alto);
  const cronologico = mediciones.slice().reverse();
  const series = {
    CO2: cronologico.filter((medicion) => medicion.tipo === 'CO2'),
    TEMPERATURA: cronologico.filter((medicion) => medicion.tipo === 'TEMPERATURA')
  };
  if (cronologico.length === 0) {
    ctx.fillStyle = '#17372f';
    ctx.font = '16px Arial';
    ctx.fillText('Aún no hay mediciones almacenadas.', 24, alto / 2);
    return;
  }
  const valores = cronologico.map((medicion) => Number(medicion.valor));
  const minimo = Math.min(...valores, 0);
  const maximo = Math.max(...valores, 1);
  const margen = 40;
  const pintarSerie = (puntos, color) => {
    if (puntos.length === 0) {
      return;
    }
    ctx.beginPath();
    ctx.strokeStyle = color;
    ctx.lineWidth = 2;
    puntos.forEach((punto, indice) => {
      const x = margen + (indice * (ancho - margen * 2)) / Math.max(puntos.length - 1, 1);
      const y = alto - margen - ((Number(punto.valor) - minimo) / (maximo - minimo || 1)) * (alto - margen * 2);
      if (indice === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });
    ctx.stroke();
  };
  pintarSerie(series.CO2, '#1b6b93');
  pintarSerie(series.TEMPERATURA, '#c45c26');
  ctx.fillStyle = '#17372f';
  ctx.font = '14px Arial';
  ctx.fillText('CO2', 16, 20);
  ctx.fillStyle = '#1b6b93';
  ctx.fillRect(50, 8, 12, 12);
  ctx.fillStyle = '#17372f';
  ctx.fillText('TEMPERATURA', 80, 20);
  ctx.fillStyle = '#c45c26';
  ctx.fillRect(190, 8, 12, 12);
}

// --> actualizarMediciones() -->
async function actualizarMediciones() {
  try {
    mostrarEstado('Cargando mediciones...', 'carga');
    const mediciones = await pedirMediciones();
    dibujarLista(mediciones);
    dibujarGrafico(mediciones);
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
  iniciarAplicacion();
}
