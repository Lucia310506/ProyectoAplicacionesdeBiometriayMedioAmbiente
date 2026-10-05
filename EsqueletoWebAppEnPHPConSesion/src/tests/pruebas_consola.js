/*
 * Fichero: pruebas_consola.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Ejecuta autocomprobaciones web al abrir Aplicacion.html.
 * Fecha: 2026-10-04
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
(async function () {
  const grupos = { web: [], logica: [], baseDatos: [] };

  // condicion: B, mensaje: Text --> comprobar() -->
  // Falla el caso actual con una explicación breve.
  function comprobar(condicion, mensaje) {
    if (!condicion) throw new Error(mensaje);
  }

  // nombre: Text, prueba: Función --> caso() -->
  // Ejecuta un caso y guarda su resultado sin detener la batería.
  async function caso(nombre, prueba) {
    console.log('Ejecutando test: ' + nombre);
    try {
      await prueba();
      grupos.web.push({ nombre, estado: 'OK' });
      console.log('OK: ' + nombre);
    } catch (error) {
      grupos.web.push({ nombre, estado: 'ERROR' });
      console.log('ERROR: ' + nombre + ' — ' + error.message);
    }
  }

  // --> probarPeticionarioREST() -->
  // Simula las respuestas y restaura fetch original incluso ante fallos.
  async function probarPeticionarioREST() {
    const fetchReal = window.fetch;
    let ruta = '';
    try {
      window.fetch = async (url) => {
        ruta = url;
        return { ok: true, text: async () => '[{"id":1,"tipo":"CO2","valor":500,"fecha":"2026-09-25T10:30:00"}]' };
      };
      await caso('PeticionarioREST devuelve mediciones de GET /mediciones', async () => {
        const datos = await pedirMediciones();
        comprobar(ruta === '/mediciones' && datos.length === 1 && datos[0].tipo === 'CO2',
          'Ruta o JSON inesperado');
      });

      for (const [nombre, respuesta, errorEsperado] of [
        ['HTTP', { ok: false, text: async () => '{"error":"HTTP simulado"}' }, 'HTTP simulado'],
        ['red', null, 'Red simulada']
      ]) {
        await caso('PeticionarioREST informa errores de ' + nombre, async () => {
          window.fetch = respuesta ? async () => respuesta : async () => { throw new Error(errorEsperado); };
          try {
            await pedirMediciones();
            throw new Error('No se notificó el error');
          } catch (error) {
            comprobar(error.message === errorEsperado, 'Mensaje de error inesperado');
          }
        });
      }
    } finally {
      window.fetch = fetchReal;
    }
  }

  // --> probarServidor() -->
  // Pide al servidor sus pruebas de lógica y BD; este borra sus filas centinela.
  async function probarServidor() {
    console.log('Ejecutando tests PHP y base de datos');
    try {
      const respuesta = await window.fetch('../tests/ejecutar_tests_consola.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ ejecutar: true }),
        signal: AbortSignal.timeout(15000)
      });
      const resultado = await respuesta.json();
      if (!respuesta.ok && !resultado.tests) throw new Error(resultado.error || 'Solicitud rechazada');
      (resultado.tests || []).forEach((test) => {
        grupos[test.grupo === 'logica' ? 'logica' : 'baseDatos'].push(test);
        console.log(test.estado + ': ' + test.nombre + (test.detalle ? ' — ' + test.detalle : ''));
      });
    } catch (error) {
      grupos.logica.push({ nombre: 'Pruebas PHP de lógica', estado: 'ERROR' });
      grupos.baseDatos.push({ nombre: 'Pruebas de base de datos', estado: 'ERROR' });
      console.log('ERROR: pruebas PHP/BD — ' + error.message);
    }
  }

  // --> imprimirResultados() -->
  // Muestra el resumen final de cada grupo de pruebas.
  function imprimirResultados() {
    console.log('RESULTADOS :');
    for (const [nombre, pruebas] of [
      ['LOGICA_FAKE_WEB', grupos.web],
      ['LOGICA_NEGOCIO_PHP', grupos.logica],
      ['BASE_DATOS_MEDICIONES', grupos.baseDatos]
    ]) {
      const ok = pruebas.filter((prueba) => prueba.estado === 'OK').length;
      console.log(nombre + '=' + (pruebas.length && ok === pruebas.length ? 'OK' : 'ERROR')
        + ' (' + ok + '/' + pruebas.length + ')');
    }
  }

  console.log('EJECUTAR TESTS');
  await probarPeticionarioREST();
  await probarServidor();
  imprimirResultados();
}());