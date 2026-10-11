/*
 * Fichero: probar_peticionario_rest.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Prueba automática del GET del PeticionarioREST web.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

const fs = require('fs');
const vm = require('vm');
const codigo = fs.readFileSync(__dirname + '/../logicaFake/PeticionarioREST.js', 'utf8');

// condicion: B, mensaje: Text --> comprobar() -->
function comprobar(condicion, mensaje) {
  if (!condicion) throw new Error(mensaje);
}

// --> probarPeticionarioRest() -->
async function probarPeticionarioRest() {
  let urlRecibida = '';
  const contexto = {
    fetch: async (url) => {
      urlRecibida = url;
      return {
        ok: true,
        text: async () => JSON.stringify([
          { id: 1, tipo: 'CO2', valor: 500, fecha: '2026-09-25T10:30:00' }
        ])
      };
    }
  };
  vm.createContext(contexto); vm.runInContext(codigo, contexto);
  const mediciones = await contexto.pedirMediciones();
  comprobar(urlRecibida === '/mediciones',
    'La ruta GET debe ser /mediciones');
  comprobar(mediciones.length === 1 && mediciones[0].tipo === 'CO2', 'Debe devolver el JSON del servidor');
  contexto.fetch = async () => ({
    ok: false,
    text: async () => JSON.stringify({ error: 'Error de red' })
  });
  let falloControlado = false;
  try { await contexto.pedirMediciones(); } catch (error) { falloControlado = error.message === 'Error de red'; }
  comprobar(falloControlado, 'Debe informar de un error HTTP');
  console.log('OK: pruebas PeticionarioREST web superadas');
}

probarPeticionarioRest();
