# Revisión del Sprint — 2026-10-10

**Autora:** Lucía Díaz Murcia  
**Rama:** `develop`  
**Commit revisado:** `df1d150` (`correcion v2`)  
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

## Pruebas Arduino añadidas

- ✅ `HolaMundoIBeacon/test.h` envía un iBeacon real con tipo reservado `PRUEBA=14` y valor centinela `0x5A3C`; comprueba que la publicidad se inicia y se detiene, y muestra cada caso y el resumen por Serial.
- ✅ Las pruebas se activan manualmente con un pulsador entre D2 y GND. La interrupción solo registra la pulsación y `loop()` ejecuta la batería fuera de la ISR.
- ✅ Android descarta el tipo `PRUEBA`, por lo que no se interpreta como medición ni se envía a REST.
- ✅ El diseño Arduino, el diseño de pruebas, el mapa de componentes y el README documentan el mismo flujo.

## Límite de ejecución

No se pudo compilar ni cargar el firmware: `arduino-cli` no está disponible y no se confirmó una placa conectada. Por tanto, la ejecución física del anuncio debe comprobarse desde Arduino IDE y un escáner BLE externo. Las pruebas PHP/BD no se ejecutaron porque PHP no está instalado. Las pruebas instrumentadas Android requieren SDK y dispositivo/emulador; aquí solo se verificaron estáticamente sus fuentes y dependencias.
