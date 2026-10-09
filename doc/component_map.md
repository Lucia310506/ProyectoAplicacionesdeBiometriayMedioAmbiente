# Mapa de componentes y correspondencia con las fuentes

La regla del agente expresa `src/xxx/` como correspondencia conceptual de cada `xxx_design.md`. En este repositorio integrado existen varias raíces de producto; las rutas concretas quedan mapeadas así:

| Diseño | Implementación |
|---|---|
| `arduino_design.md` | `HolaMundoIBeacon/` |
| `android_design.md` (lógica fake + contrato REST Android) | `BTLEAlumnos2021hechoapp2/app/src/main/java/` (`LogicaFake.java`, `PeticionarioREST.java`, servicio BLE) |
| `web_rest_design.md` | `EsqueletoWebAppEnPHPConSesion/src/rest/` |
| `web_logic_design.md` | `EsqueletoWebAppEnPHPConSesion/src/logica/` |
| `web_rest_client_design.md` | `EsqueletoWebAppEnPHPConSesion/src/logicaFake/` |
| `web_ux_design.md` | `EsqueletoWebAppEnPHPConSesion/src/ux/` |
| `database_design.md` | `EsqueletoWebAppEnPHPConSesion/bbdd/` |
| `database_connection_design.md` | `EsqueletoWebAppEnPHPConSesion/src/BBDD/` |
| `tests_design.md` | `EsqueletoWebAppEnPHPConSesion/src/tests/` y `BTLEAlumnos2021hechoapp2/app/src/` pruebas |

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
| Android | `LogicaFake.java` | Valida tipo y valor; delega la consulta GET. |
| Android | `PeticionarioREST.java`, `ConfiguracionRest.java` | Cliente HTTP y URL base del endpoint. |
| Android | `PruebasRESTBoton.java` | Simula GET, POST, 201 y HTTP 500 para el botón de tests. |
| PHP REST | `src/rest/mediciones.php` | Adapta entrada/salida HTTP a las operaciones lógicas. |
| PHP lógica | `src/logica/mediciones.php` | Valida, persiste, consulta y normaliza. |
| PHP DB | `src/BBDD/ConexionMediciones.php` | Resuelve entorno y construye PDO. El archivo `ConfiguracionProduccion.php` es local/ignorado. |
| Web REST | `src/logicaFake/PeticionarioREST.js` | Cliente de lectura GET desde el navegador. |
| Web UX | `src/ux/Aplicacion.html`, `Aplicacion.js`, hojas de estilo | Arranque, presentación de mediciones y estados de interfaz. |
| Pruebas web | `src/tests/pruebas_consola.js` | Orquesta casos del navegador solo al pulsar el botón. |
| Pruebas PHP | `src/tests/ejecutar_tests_consola.php`, `probar_base_datos.php`, `probar_rest.php` | Comprueban lógica, REST y BD exclusivamente contra el entorno de prueba. |
| SQL | `bbdd/Estructura.sql`, `bbdd/datos.sql` | Creación de esquema y datos opcionales de ejemplo. |

## Direcciones de dependencia

```text
Arduino → BLE → Android → HTTP POST → REST PHP → lógica → PDO → MySQL
UX web → cliente REST JavaScript → HTTP GET → REST PHP → lógica → PDO → MySQL
Pruebas web del navegador → ejecutor PHP de mismo origen → lógica/REST → BD de pruebas
Botón Android → lógica fake + batería REST simulada → Logcat
Botón Arduino → bandera de interrupción → loop() → test.h → Serial
```

Los directorios `src/xxx/` requeridos literalmente por la plantilla no se han duplicado: las implementaciones ya viven en las raíces históricas anteriores. Este mapa documenta la equivalencia sin mover código fuente ni romper Gradle, rutas PHP o el sketch Arduino.

