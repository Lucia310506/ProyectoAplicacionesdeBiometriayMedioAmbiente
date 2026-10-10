# Diseño de las pruebas del proyecto

## Component Design

El componente ejecuta pruebas automatizadas aisladas contra la base de pruebas: lógica de base de datos, endpoint REST y cliente/UX JavaScript con dependencias HTTP simuladas. No usa la base de producción.

### Interfaces lógicas

- `condicion: B, mensaje: Text --> comprobar() -->`
- `--> probarBaseDatos() -->`
- `--> probarRest() -->`
- `--> probarPeticionarioRest() -->`
- `--> probarUx() -->`

### Diseño global del ejecutor web

`pruebas_consola.js` es un módulo JavaScript global del navegador. Conserva referencias temporales mientras ejecuta los casos y restaura la vista al terminar. Solo empieza al pulsar el botón.

```text
┌──────────────────────────── pruebas_consola.js ────────────────────────────┐
│ comprobar(condicion, mensaje)                                               │
│ caso(nombre, prueba, resultados)                                            │
│ probarPeticionarioREST(resultados)                                          │
│ probarUX(resultados)                                                        │
│ probarServidor(resultados)                                                  │
│ imprimirResultados(grupos): Boolean                                         │
│ ejecutarPruebasWeb(): Promise<void>  ← click del botón                      │
└────────────────────────────────────────────────────────────────────────────┘
```

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `comprobar(condicion, mensaje): void` | `condicion: B, mensaje: Text --> comprobar() -->` | Detiene el caso con un error legible si la condición no se cumple. |
| `caso(nombre, prueba, resultados): Promise<void>` | `nombre: Text, prueba: Funcion, resultados: Test[] --> caso() -->` | Ejecuta un caso, registra OK/ERROR y deja continuar la batería. |
| `probarPeticionarioREST(resultados): Promise<void>` | `resultados: Test[] --> probarPeticionarioREST() -->` | Simula GET correcto, error HTTP y error de red. |
| `probarUX(resultados): Promise<void>` | `resultados: Test[] --> probarUX() -->` | Comprueba carga, representación y error; restaura DOM y función sustituida. |
| `probarServidor(resultados): Promise<void>` | `resultados: GruposTest --> probarServidor() -->` | Llama al ejecutor PHP del mismo origen y agrega resultados de lógica, REST y BD. |
| `imprimirResultados(grupos): bool` | `grupos: Dict --> imprimirResultados() --> B` | Imprime recuentos en consola y devuelve si todas las baterías pasaron. |
| `ejecutarPruebasWeb(): Promise<void>` | `click: Boton --> ejecutarPruebasWeb() -->` | Evita dobles ejecuciones, coordina pruebas y reactiva el botón al terminar. |

### Diseño global de pruebas Android

`MainActivity` proporciona el botón que activa los casos y `PruebasRESTBoton` usa conexiones HTTP falsas. La validación fake se ejecuta en el hilo principal cuando se pulsa. Los casos de REST simulado usan el cliente asíncrono y devuelven el resultado mediante callback. No escriben en MySQL.

```text
Botón → MainActivity.botonEjecutarTestsPulsado()
          ├─ comprobarLogicaFake() → LogicaFake.guardarMediciones()
          └─ comprobarRest() → PruebasRESTBoton.ejecutar(callback)
                                ├─ GET simulado
                                ├─ POST + HTTP 201 simulado
                                └─ error HTTP simulado
```

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `botonEjecutarTestsPulsado(View vista): void` | `vista: View --> botonEjecutarTestsPulsado() -->` | Inicia la batería al pulsar, deshabilita el botón y registra el inicio en Logcat. |
| `comprobarLogicaFake(): void` | `--> comprobarLogicaFake() --> B` | Comprueba CO2/TEMPERATURA válidos y rechazos de tipo y NaN. |
| `comprobarRechazoLogica(tipo, valor, nombre): void` | `tipo: Text, valor: R, nombre: Text --> comprobarRechazoLogica() -->` | Verifica que una entrada inválida lanza `IllegalArgumentException`. |
| `comprobarRest(): void` | `--> comprobarRest() -->` | Lanza casos REST simulados y escribe el resumen final en Logcat. |
| `PruebasRESTBoton.ejecutar(resultado): void` | `callback: ResultadoFinal --> ejecutar() -->` | Ejecuta la batería HTTP falsa sin servidor ni base reales. |
| `Bateria.probarGet(): void` | `--> probarGet() -->` | Comprueba GET, URL, respuesta y callback. |
| `Bateria.probarPost(): void` | `--> probarPost() -->` | Comprueba POST, JSON tipo/valor y respuesta HTTP 201. |
| `Bateria.probarErrorHttp(): void` | `--> probarErrorHttp() -->` | Comprueba la entrega de una respuesta HTTP de error. |

### Matriz de cobertura

| Batería | Activación | Casos principales | Destino de los resultados | Efecto sobre datos |
|---|---|---|---|---|
| Web navegador | Botón de `Aplicacion.html` | GET del cliente simulado; contrato de `LogicaFake.js`; carga/éxito/error de la UX; lógica PHP; REST; esquema, selección de entorno y limpieza de BD. | Consola del navegador y estado del botón. | El grupo PHP borra y recrea filas solo en la BD configurada como pruebas. |
| Android lógica | Botón de pruebas Android | CO2 y temperatura válidos; tipo inválido; `NaN`. | Logcat con tags `TESTS_APP` y `TEST_LOGICAFake`. | No accede a la BD. |
| Android REST | Parte del mismo botón | GET, ruta/método/JSON, POST con HTTP 201 y HTTP 500. | Logcat con `TEST_PETICIONARIO_REST` y resumen en `TESTS_APP`. | Transporte simulado; no accede al servidor. |
| Android beacon de prueba | Botón **Buscar beacon de prueba** | UUID de proyecto, prefijo iBeacon, Major con tipo 14 y Minor `0x5A3C`; búsqueda con tiempo límite. | Estado visible en la pantalla y Logcat con `TEST_BEACON`. | Solo observa BLE; no llama a lógica de mediciones ni a REST. |
| Arduino BLE | Pulsador D2-GND; `loop()` consume la solicitud | Beacon `PRUEBA=14`, valor centinela, publicidad activa y detención posterior. | Monitor serie a 115200 baudios; beacon visible tres segundos para escaneo externo. | Android descarta el tipo de prueba; no llega a REST ni a la base de datos. |

### Orden de una ejecución web

1. El listener del botón impide una segunda ejecución concurrente y desactiva el control.
2. Los casos del cliente REST sustituyen `window.fetch` por respuestas preparadas y lo restauran en `finally`.
3. Los casos de UX sustituyen la función de petición y guardan el estado/filas actuales; restauran el DOM incluso si falla un caso.
4. La batería del servidor envía POST same-origin al ejecutor PHP. El PHP exige `MEDICIONES_ENTORNO=pruebas`, obtiene PDO, limpia la tabla, comprueba el esquema, inserta mediciones centinela y verifica lógica/REST.
5. En el bloque `finally`, PHP limpia de nuevo la tabla y agrega el resultado de limpieza.
6. El navegador agrupa resultados y muestra conteos `OK (correctos/total)` o `ERROR` en consola; reactiva el botón.

### Requisitos y límites de seguridad para las pruebas

- Para que la batería web sea completa, el botón debe abrirse desde el mismo origen del servidor PHP; no basta con abrir el HTML como archivo local.
- Debe configurarse una base exclusiva para pruebas antes de ejecutar la parte PHP. La limpieza elimina todas las filas de `mediciones` de esa base.
- Si falta conexión/configuración PHP, los casos de lógica, REST o BD se registran como error; los casos JavaScript simulados pueden seguir siendo útiles.
- Las pruebas Android del botón usan `HttpURLConnection` simulado, sin depender de la disponibilidad de la API.
- `PeticionarioRESTTest` es una prueba instrumentada independiente que usa `MockWebServer` local para verificar GET, POST, URL, cuerpo JSON y callback. Se ejecuta desde Android Studio o con `:app:connectedDebugAndroidTest` cuando hay un dispositivo o emulador conectado.
- `test.h` emite un beacon real `PRUEBA=14` durante tres segundos, verifica que la publicidad BLE se inicia y se detiene y presenta el resultado por Serial. La ISR solo registra la pulsación; `loop()` ejecuta la batería.
- La búsqueda Android dura como máximo diez segundos y acepta únicamente el UUID del proyecto, el formato iBeacon, Major de tipo 14 y Minor `0x5A3C`; el contador ocupa el byte bajo de Major.

## Design Clarifications

- Las pruebas se activan manualmente desde la web, la aplicación Android y el pulsador Arduino. Las pruebas web de servidor necesitan una BD aislada; Android usa conexiones simuladas.

## General Rules

- **Programming Language:** PHP/JavaScript para pruebas web, Java para pruebas de botón Android y C++ para firmware.
- **Function/Method Headers:** Cada cabecera debe incluir el diseño lógico en un bloque de comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro y autoexplicativo; comentarios inline mínimos.
- **Automated Testing:** Aislar datos de prueba y cubrir flujos críticos y errores esperados.
