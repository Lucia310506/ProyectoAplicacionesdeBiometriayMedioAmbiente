/*
 * Fichero: pruebas_consola.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Ejecuta las pruebas web al iniciar Aplicacion.html y muestra resultados con console.log.
 * Fecha: 2026-10-03
 * Copyright (c) 2026 Lucía Díaz Murcia
 */

(function () {
  const resultados = { fake: [], logica: [], baseDatos: [] };

  function comprobar(condicion, mensaje) {
    if (!condicion) throw new Error(mensaje);
  }

  async function ejecutarCaso(grupo, nombre, comprobacion) {
    console.log('Ejecutando test: ' + nombre);
    try {
      await comprobacion();
      resultados[grupo].push({ nombre, estado: 'OK' });
      console.log('OK: ' + nombre);
    } catch (error) {
      resultados[grupo].push({ nombre, estado: 'ERROR', detalle: error.message });
      console.log('ERROR: ' + nombre + ' — ' + error.message);
    }
  }

  async function ejecutarTestsFakeWeb() {
    const fetchOriginal = window.fetch;
    let urlSolicitada = '';
    try {
      window.fetch = async (url) => {
        urlSolicitada = url;
        return {
          ok: true,
          text: async () => JSON.stringify([
            { id: 1, tipo: 'CO2', valor: 500, fecha: '2026-09-25T10:30:00' }
          ])
        };
      };
      await ejecutarCaso('fake', 'PeticionarioREST devuelve mediciones de GET /mediciones', async () => {
        const mediciones = await pedirMediciones();
        comprobar(urlSolicitada === '/mediciones', 'La ruta solicitada no es /mediciones');
        comprobar(mediciones.length === 1 && mediciones[0].tipo === 'CO2', 'No devolvió las mediciones JSON esperadas');
      });

      window.fetch = async () => ({
        ok: false,
        text: async () => JSON.stringify({ error: 'Error HTTP simulado' })
      });
      await ejecutarCaso('fake', 'PeticionarioREST informa errores HTTP', async () => {
        let mensaje = '';
        try { await pedirMediciones(); } catch (error) { mensaje = error.message; }
        comprobar(mensaje === 'Error HTTP simulado', 'No propagó el error HTTP');
      });

      window.fetch = async () => { throw new Error('Error de red simulado'); };
      await ejecutarCaso('fake', 'PeticionarioREST informa errores de red', async () => {
        let mensaje = '';
        try { await pedirMediciones(); } catch (error) { mensaje = error.message; }
        comprobar(mensaje === 'Error de red simulado', 'No propagó el error de red');
      });
    } finally {
      window.fetch = fetchOriginal;
    }
  }

  async function ejecutarTestsServidor() {
    console.log('Ejecutando tests PHP de lógica de negocio y base de datos');
    let tiempoLimite;
    try {
      const controlador = new AbortController();
      tiempoLimite = window.setTimeout(() => controlador.abort(), 15000);
      const respuesta = await window.fetch('../tests/ejecutar_tests_consola.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ ejecutar: true }),
        signal: controlador.signal
      });
      window.clearTimeout(tiempoLimite);
      const resultado = await respuesta.json();

      if (!respuesta.ok && !resultado.tests) {
        throw new Error(resultado.error || 'El servidor rechazó la ejecución');
      }
      (resultado.tests || []).forEach((test) => {
        const grupo = test.grupo === 'logica' ? 'logica' : 'baseDatos';
        resultados[grupo].push(test);
        console.log(test.estado + ': ' + test.nombre + (test.detalle ? ' — ' + test.detalle : ''));
      });
    } catch (error) {
      window.clearTimeout(tiempoLimite);
      resultados.logica.push({ nombre: 'Pruebas PHP de lógica de negocio', estado: 'ERROR', detalle: error.message });
      resultados.baseDatos.push({ nombre: 'Pruebas de base de datos', estado: 'ERROR', detalle: error.message });
      console.log('ERROR: pruebas PHP/BD — ' + error.message);
    }
  }

  function imprimirResultados() {
    const grupos = [
      ['LOGICA_FAKE_WEB', resultados.fake],
      ['LOGICA_NEGOCIO_PHP', resultados.logica],
      ['BASE_DATOS_MEDICIONES', resultados.baseDatos]
    ];
    console.log('RESULTADOS :');
    grupos.forEach(([nombre, tests]) => {
      const estado = tests.length > 0 && tests.every((test) => test.estado === 'OK') ? 'OK' : 'ERROR';
      console.log(nombre + '=' + estado + ' (' + tests.filter((test) => test.estado === 'OK').length + '/' + tests.length + ')');
    });
  }

  async function iniciarTests() {
    console.log('EJECUTAR TESTS');
    await ejecutarTestsFakeWeb();
    await ejecutarTestsServidor();
    imprimirResultados();
  }

  window.promesasPruebasInicio = iniciarTests();
}());

