# Revisión del Sprint — 2026-10-13

**Autora:** Lucía Díaz Murcia  
**Rama:** `develop`  
**Commit revisado:** `9fe6fc8` (`Merge branch 'master' into develop`) + ajustes locales de verificación  
**Agente:** `Agente-Revisar-Sprint-v2`

## Comprobaciones aprobadas

- ✅ `author.md` está en la raíz e identifica a la autora; el informe usa su nombre.
- ✅ `doc/` contiene diseños por componente, la tríada backend (`communication_design.md`, `business_logic_design.md`, `database_design.md`) y `frontend_business_logic_design.md`.
- ✅ Los diseños incluyen `Component Design`, `Design Clarifications` y `General Rules`, con lenguaje, formato de cabeceras, legibilidad y pruebas.
- ✅ `database_design.md` sigue el formato relacional exigido y coincide con la tabla `mediciones`.
- ✅ `src/` contiene código agrupado en `communication/`, `business_logic/`, `frontend_business_logic/`, `gui/`, además de `database/`, `android/`, `arduino/` y `tests/`.
- ✅ La lógica backend usa tipos lógicos de dominio y persistencia; su diseño y código no dependen de comunicación ni de transporte.
- ✅ El flujo de dependencias es comunicación → negocio → base de datos. El código de negocio solo importa la conexión PDO.
- ✅ La GUI consume `mostrarMediciones()` y no contiene llamadas directas a `fetch`, `XMLHttpRequest` ni WebSocket. La fachada frontend encapsula el cliente REST.
- ✅ La firma lógica de `mostrarMediciones()` coincide en frontend y backend: `--> mostrarMediciones() --> [Medicion]`.
- ✅ Las pruebas JavaScript de UX y del cliente REST pasan: `node src/tests/probar_ux.js` y `node src/tests/probar_peticionario_rest.js`.
- ✅ La prueba BLE de Arduino se activa ahora enviando `t` por el monitor serie; se eliminó el pulsador D2-GND y su interrupción. El diseño, el README, los tests y el mapa de componentes se actualizaron para mantener coherencia.
- ✅ Durante la revisión se ajustó `src/gui/Aplicacion.js` para invocar `mostrarMediciones()` de forma global, restaurando la compatibilidad con el módulo cargado por `Aplicacion.html` y con el harness de `probar_ux.js`.
- ✅ La ruta de inicio web quedó corregida en `.htaccess`: apunta a `src/gui/Aplicacion.html` en lugar de una carpeta inexistente.
- ✅ El ejecutor de pruebas PHP exige `MEDICIONES_ENTORNO=pruebas` y rechaza explícitamente el entorno de producción antes de conectar o modificar datos.
- ✅ Los tests (JavaScript, PHP y la prueba instrumentada Android) usan bloques de comentario delimitados por `--------------------` en sus encabezados de función, incluidos `probar_peticionario_rest.js` y `probar_ux.js`.
- ✅ Se eliminaron comentarios descriptivos duplicados en `Medidor.h` y `Publicador.h`.
- ✅ La clave primaria de `mediciones` es compuesta: `(dispositivo_id, fecha)`, con fecha generada por MySQL en microsegundos; el POST incluye `dispositivo_id` y `lugar`.
- ✅ El POST exige `dispositivo_id`, `tipo`, `valor` y `lugar`, tal como describe ahora la documentación REST.
- ✅ El modelo `Medicion` quedó unificado en los diseños de negocio, REST, frontend y UX como `(dispositivo_id, fecha, tipo, valor, lugar)`.
- ✅ `pruebas_consola.js` ya no incorpora un cliente REST alternativo basado en `fetch` directo; detecta explícitamente la ausencia de `PeticionarioREST.js` o `LogicaFake.js`.
- ✅ La respuesta HTTP 500 ya no devuelve el mensaje interno de la excepción al cliente; solo lo registra en el log PHP.

## Pruebas Arduino añadidas

- ✅ `HolaMundoIBeacon/test.h` envía un iBeacon real con tipo reservado `PRUEBA=14` y valor centinela `0x5A3C`; comprueba que la publicidad se inicia y se detiene, y muestra cada caso y el resumen por Serial.
- ✅ Las pruebas se activan manualmente enviando `t` o `T` por el monitor serie. `procesarComandoSerial()` registra la solicitud y `loop()` ejecuta la batería fuera de la recepción serie.
- ✅ Android descarta el tipo `PRUEBA`, por lo que no se interpreta como medición ni se envía a REST.
- ✅ El diseño Arduino, el diseño de pruebas, el mapa de componentes y el README documentan el mismo flujo.

## Límite de ejecución

No se pudo compilar ni cargar el firmware: `arduino-cli` no está disponible y no se confirmó una placa conectada. Por tanto, la ejecución física del anuncio debe comprobarse desde Arduino IDE y un escáner BLE externo. Las pruebas PHP/BD no se ejecutaron porque PHP no está instalado. Las pruebas instrumentadas Android requieren SDK y dispositivo/emulador; aquí solo se verificaron estáticamente sus fuentes y dependencias.