# Mapa de componentes y correspondencia con las fuentes

Este repositorio integrado conserva las raíces de proyecto nativas de Arduino IDE, Android Studio y la aplicación PHP. La carpeta raíz `src/` contiene ahora las áreas exigidas por componente y un mapa hacia las fuentes reales, que permanecen en sus proyectos nativos para conservar compilación y despliegue.

| Carpeta raíz | Componente | Mapa |
|---|---|---|
| `src/communication/` | Comunicación HTTP | `src/communication/README.md` |
| `src/business_logic/` | Lógica de negocio | `src/business_logic/README.md` |
| `src/frontend_business_logic/` | Lógica de negocio de interfaz | `src/frontend_business_logic/README.md` |
| `src/gui/` | Interfaz de usuario | `src/gui/README.md` |

| Diseño | Implementación |
|---|---|
| `arduino_design.md` | `HolaMundoIBeacon/` |
| `android_design.md` (lógica fake + contrato REST Android) | `BTLEAlumnos2021hechoapp2/app/src/main/java/` (`LogicaFake.java`, `PeticionarioREST.java`, servicio BLE) |
| `web_rest_design.md` | `EsqueletoWebAppEnPHPConSesion/src/rest/` |
| `web_logic_design.md` | `EsqueletoWebAppEnPHPConSesion/src/logica/` |
| `communication_design.md` | `EsqueletoWebAppEnPHPConSesion/src/rest/` (adaptador HTTP de mediciones) |
| `business_logic_design.md` | `EsqueletoWebAppEnPHPConSesion/src/logica/` (reglas y acceso a datos de mediciones) |
| `web_rest_client_design.md` | `EsqueletoWebAppEnPHPConSesion/src/logicaFake/` |
| `frontend_business_logic_design.md` | `EsqueletoWebAppEnPHPConSesion/src/logicaFake/LogicaFake.js` |
| `web_ux_design.md` | `EsqueletoWebAppEnPHPConSesion/src/ux/` |
| `database_design.md` | `EsqueletoWebAppEnPHPConSesion/bbdd/` |
| `database_connection_design.md` | `EsqueletoWebAppEnPHPConSesion/src/BBDD/` |
| `tests_design.md` | `EsqueletoWebAppEnPHPConSesion/src/tests/`, `BTLEAlumnos2021hechoapp2/app/src/main/` (botón) y `app/src/androidTest/` (MockWebServer) |

## Mapa detallado de fuentes

| Área | Archivo/carpeta | Función en la arquitectura |
|---|---|---|
| Arduino | `HolaMundoIBeacon/HolaMundoIBeacon.ino` | Define objetos compartidos, `setup()`, `loop()` y atención diferida del botón de pruebas. |
| Arduino | `HolaMundoIBeacon/Medidor.h` | Devuelve los valores simulados CO2 y temperatura. |
| Arduino | `HolaMundoIBeacon/Publicador.h` | Codifica el identificador de tipo/contador y anuncia Major/Minor. |
| Arduino | `HolaMundoIBeacon/EmisoraBLE.h`, `ServicioEnEmisora.h` | Configuran el adaptador BLE/Bluefruit, anuncios y servicios GATT auxiliares. |
| Arduino | `HolaMundoIBeacon/LED.h`, `PuertoSerie.h` | Controlan indicadores y salida de depuración. |
| Arduino | `HolaMundoIBeacon/test.h` | Comprueba valores simulados cuando el usuario pulsa el botón. |
| Android | `MainActivity.java` | Pantalla, permisos, inicio/detención del servicio y botón de test. |
| Android | `ServicioEscuharBeacons.java` | Escaneo en primer plano, recepción, filtrado y despacho de beacons. |
| Android | `TramaIBeacon.java`, `Utilidades.java` | Descomponen la trama y convierten bytes, UUID, números y texto. |
| Android | `LogicaFake.java` | Valida tipo y valor sin dependencias HTTP ni callbacks de transporte. |
| Android | `PeticionarioREST.java`, `ConfiguracionRest.java` | Cliente HTTP y URL base del endpoint. |
| Android | `PruebasRESTBoton.java` | Simula GET, POST, 201 y HTTP 500 para el botón de tests. |
| PHP REST | `src/rest/mediciones.php` | Adapta entrada/salida HTTP a las operaciones lógicas. |
| PHP lógica | `src/logica/mediciones.php` | Valida, persiste, consulta y normaliza. |
| PHP DB | `src/BBDD/ConexionMediciones.php` | Resuelve entorno y construye PDO. El archivo `ConfiguracionProduccion.php` es local/ignorado. |
| Web REST | `src/logicaFake/PeticionarioREST.js` | Cliente de lectura GET desde el navegador. |
| Web lógica frontend | `src/logicaFake/LogicaFake.js` | Fachada de dominio que la UX usa para pedir una lista de mediciones y validar su forma. |
| Web UX | `src/ux/Aplicacion.html`, `Aplicacion.js`, hojas de estilo | Arranque, presentación de mediciones y estados de interfaz. |
| Pruebas web | `src/tests/pruebas_consola.js` | Orquesta casos del navegador solo al pulsar el botón. |
| Pruebas PHP | `src/tests/ejecutar_tests_consola.php`, `probar_base_datos.php`, `probar_rest.php` | Comprueban lógica, REST y BD exclusivamente contra el entorno de prueba. |
| SQL | `bbdd/Estructura.sql`, `bbdd/datos.sql` | Creación de esquema y datos opcionales de ejemplo. |

## Direcciones de dependencia

```text
Arduino → BLE → Android → HTTP POST → REST PHP → lógica → PDO → MySQL
UX web → cliente REST JavaScript → HTTP GET → REST PHP → lógica → PDO → MySQL
Pruebas web del navegador → ejecutor PHP de mismo origen → lógica/REST → BD de pruebas
Botón Android → validación de dominio + batería REST simulada → Logcat
Botón Arduino → bandera de interrupción → loop() → test.h → Serial
```

Las carpetas raíz cumplen la organización literal por componente. Cada README de `src/` apunta a la implementación que permanece dentro de su proyecto ejecutable; no se mantienen copias paralelas del código fuente.

