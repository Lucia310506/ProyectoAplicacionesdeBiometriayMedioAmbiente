/*
 * Fichero: pruebas_consola.js
 * Autor: Lucía Díaz Murcia
 * Descripción: Ejecuta las pruebas web solo cuando se pulsa el botón.
 * Fecha: 2026-10-09
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
(function () {
  const boton = document.getElementById('boton-tests');
  const estado = document.getElementById('estado-tests');
  let enCurso = false;

  // condicion: B, mensaje: Text --> comprobar() -->
  // Falla el caso actual con una explicación breve.
  function comprobar(condicion, mensaje) {
    if (!condicion) throw new Error(mensaje);
  }

  // nombre: Text, prueba: Funcion, resultados: [Dict] --> caso() -->
  // Ejecuta un caso y guarda su resultado sin detener las demás pruebas.
  async function caso(nombre, prueba, resultados) {
    console.log('Ejecutando test: ' + nombre);
    try {
      await prueba();
      resultados.push({ nombre, estado: 'OK' });
      console.log('OK: ' + nombre);
    } catch (error) {
      resultados.push({ nombre, estado: 'ERROR' });
      console.log('ERROR: ' + nombre + ' — ' + error.message);
    }
  }

  // resultados: [Dict] --> probarPeticionarioREST() -->
  // Simula GET, errores HTTP y errores de red del cliente web.
  async function probarPeticionarioREST(resultados) {
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
      }, resultados);

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
        }, resultados);
      }
    } finally {
      window.fetch = fetchReal;
    }
  }

  // resultados: [Dict] --> probarUX() -->
  // Comprueba carga, renderizado de filas y error; restaura la vista al terminar.
  async function probarUX(resultados) {
    const estado = document.getElementById('estado');
    const cuerpo = document.getElementById('cuerpo-mediciones');
    const estadoOriginal = { texto: estado.textContent, clase: estado.className };
    const filasOriginales = Array.from(cuerpo.childNodes).map((fila) => fila.cloneNode(true));
    const pedirOriginal = window.pedirMediciones;
    try {
      let terminarCarga;
      window.pedirMediciones = () => new Promise((resolver) => { terminarCarga = resolver; });
      const actualizacion = actualizarMediciones();
      await caso('UX muestra el estado de carga', async () => {
        comprobar(estado.className === 'carga' && estado.textContent.includes('Cargando'),
          'No mostró el estado de carga');
      }, resultados);
      terminarCarga([{ id: 1, tipo: 'CO2', valor: 500, fecha: '2026-09-25T10:30:00' }]);
      await actualizacion;

      await caso('UX dibuja las mediciones recibidas por GET', async () => {
        comprobar(cuerpo.rows.length === 1 && cuerpo.rows[0].cells[0].textContent === 'CO2',
          'No dibujó la fila recibida');
        comprobar(estado.className === 'ok', 'No mostró estado de éxito');
      }, resultados);

      window.pedirMediciones = async () => { throw new Error('Fallo simulado'); };
      await actualizarMediciones();
      await caso('UX muestra el error de la consulta', async () => {
        comprobar(estado.className === 'error' && estado.textContent === 'Fallo simulado',
          'No mostró el error recibido');
      }, resultados);
    } finally {
      window.pedirMediciones = pedirOriginal;
      estado.textContent = estadoOriginal.texto;
      estado.className = estadoOriginal.clase;
      cuerpo.replaceChildren(...filasOriginales);
    }
  }

  // resultados: Dict --> probarServidor() -->
  // Ejecuta las pruebas PHP sobre la base aislada configurada para pruebas.
  async function probarServidor(resultados) {
    console.log('Ejecutando pruebas PHP y base de datos');
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
        resultados[test.grupo].push(test);
        console.log(test.estado + ': ' + test.nombre + (test.detalle ? ' — ' + test.detalle : ''));
      });
    } catch (error) {
      resultados.logica.push({ nombre: 'Pruebas PHP de lógica', estado: 'ERROR' });
      resultados.rest.push({ nombre: 'Pruebas REST', estado: 'ERROR' });
      resultados.baseDatos.push({ nombre: 'Pruebas de base de datos', estado: 'ERROR' });
      console.log('ERROR: pruebas PHP/BD — ' + error.message);
    }
  }

  // grupos: Dict --> imprimirResultados() --> Texto
  // Imprime los resúmenes y devuelve si todos los casos han pasado.
  function imprimirResultados(grupos) {
    console.log('RESULTADOS :');
    let todoCorrecto = true;
    for (const [nombre, pruebas] of [
      ['LOGICA_FAKE_WEB', grupos.web],
      ['UX_WEB', grupos.ux],
      ['LOGICA_NEGOCIO_PHP', grupos.logica],
      ['SERVIDOR_REST', grupos.rest],
      ['BASE_DATOS_MEDICIONES', grupos.baseDatos]
    ]) {
      const correctos = pruebas.filter((prueba) => prueba.estado === 'OK').length;
      const correcto = pruebas.length > 0 && correctos === pruebas.length;
      todoCorrecto = todoCorrecto && correcto;
      console.log(nombre + '=' + (correcto ? 'OK' : 'ERROR') + ' (' + correctos + '/' + pruebas.length + ')');
    }
    return todoCorrecto;
  }

  // --> ejecutarPruebasWeb() -->
  // Ejecuta la batería web completa al solicitarla desde el botón.
  async function ejecutarPruebasWeb() {
    if (enCurso) return;
    enCurso = true;
    boton.disabled = true;
    estado.textContent = 'Ejecutando pruebas; mira la consola del navegador.';
    const grupos = { web: [], ux: [], logica: [], rest: [], baseDatos: [] };

    console.log('EJECUTAR TESTS');
    try {
      await probarPeticionarioREST(grupos.web);
      await probarUX(grupos.ux);
      await probarServidor(grupos);
      estado.textContent = imprimirResultados(grupos)
        ? 'Todas las pruebas han terminado correctamente.'
        : 'Hay pruebas con errores. Revisa la consola del navegador.';
    } catch (error) {
      console.log('ERROR: fallo inesperado en las pruebas — ' + error.message);
      estado.textContent = 'No se pudieron completar las pruebas. Revisa la consola.';
    } finally {
      enCurso = false;
      boton.disabled = false;
    }
  }

  boton.addEventListener('click', ejecutarPruebasWeb);
})();
