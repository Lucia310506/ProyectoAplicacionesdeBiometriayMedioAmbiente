/*
 * Fichero: probar_ux.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Pruebas de interfaz con respuestas REST simuladas.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

const fs = require('fs');
const vm = require('vm');
const codigo = fs.readFileSync(__dirname + '/../ux/Aplicacion.js', 'utf8');
const codigoLogica = fs.readFileSync(__dirname + '/../logicaFake/LogicaFake.js', 'utf8');

// condicion: B, mensaje: Text --> comprobar() -->
function comprobar(condicion, mensaje) {
  if (!condicion) throw new Error(mensaje);
}

// html: Text --> crearDocumento() --> Documento
function crearDocumento() {
  const estado = { textContent: 'Cargando mediciones...', className: '' };
  const cuerpo = {
    hijos: [],
    replaceChildren() { this.hijos = []; },
    appendChild(nodo) { this.hijos.push(nodo); }
  };
  return {
    getElementById(id) {
      if (id === 'estado') return estado;
      if (id === 'cuerpo-mediciones') return cuerpo;
      if (id === 'grafico') return { width: 900, height: 280, getContext: () => null };
      return null;
    },
    createElement() {
      return {
        innerHTML: '',
        textContent: '',
        hijos: [],
        appendChild(nodo) { this.hijos.push(nodo); }
      };
    },
    estado,
    cuerpo
  };
}

// --> probarUx() -->
async function probarUx() {
  const documento = crearDocumento();
  let medicionesSimuladas = [];
  let debeFallar = false;
  const contexto = {
    document: documento,
    window: undefined,
    setInterval() {},
    pedirMediciones: async () => {
      if (debeFallar) throw new Error('Fallo simulado');
      return medicionesSimuladas;
    }
  };
  vm.createContext(contexto);
  vm.runInContext(codigoLogica, contexto);
  vm.runInContext(codigo, contexto);

  comprobar(documento.estado.textContent === 'Cargando mediciones...', 'Renderizado inicial de carga');
  await contexto.actualizarMediciones();
  comprobar(documento.estado.className === 'carga' || documento.estado.className === 'ok',
    'Debe mostrar estado de carga o vacío');
  comprobar(documento.estado.textContent.includes('No hay mediciones'), 'Lista vacía visible');

  medicionesSimuladas = [
    { id: 1, tipo: 'CO2', valor: 500, fecha: '2026-09-25T10:30:00' }
  ];
  await contexto.actualizarMediciones();
  comprobar(documento.cuerpo.hijos.length === 1, 'Debe renderizar las filas del GET');
  comprobar(documento.estado.className === 'ok', 'Estado de éxito');
  comprobar(documento.estado.textContent.includes('1 mediciones'), 'Éxito con recuento');

  debeFallar = true;
  await contexto.actualizarMediciones();
  comprobar(documento.estado.className === 'error', 'Estado de error');
  comprobar(documento.estado.textContent === 'Fallo simulado', 'Debe mostrar el error REST');
  console.log('OK: pruebas UX superadas');
}

probarUx();
