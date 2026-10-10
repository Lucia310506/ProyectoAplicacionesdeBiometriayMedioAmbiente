# Mapa de componentes y correspondencia con las fuentes

El repositorio conserva los proyectos nativos de Arduino IDE, Android Studio y la aplicación PHP. La carpeta raíz `src/` reúne fuentes por componente, incluidas copias de trabajo para respetar la estructura requerida. La aplicación web desplegable mantiene fuentes equivalentes dentro de `EsqueletoWebAppEnPHPConSesion/`.

| Carpeta raíz | Componente | Implementación fuente |
|---|---|---|
| `src/communication/` | Cliente web y adaptador PHP | `PeticionarioREST.js`, `mediciones.php` |
| `src/business_logic/` | Reglas de negocio PHP | `mediciones.php` |
| `src/frontend_business_logic/` | Fachada de dominio web | `LogicaFake.js` |
| `src/database/` | Conexión PDO y esquema | `ConexionMediciones.php`, `Estructura.sql` |
| `src/gui/` | Interfaz web | `Aplicacion.html`, `Aplicacion.js` |
| `src/android/` | Aplicación Android | Clases Java del proyecto móvil |
| `src/arduino/` | Firmware Arduino | Sketch, cabeceras y `test.h` |
| `src/tests/` | Pruebas web | JavaScript y PHP |

Las copias de despliegue web conservan la misma separación dentro de `EsqueletoWebAppEnPHPConSesion/src/`: `logica/`, `rest/`, `logicaFake/`, `BBDD/`, `ux/` y `tests/`. Android y Arduino conservan sus configuraciones de compilación en sus carpetas nativas.

| Diseño | Implementación fuente principal |
|---|---|
| `arduino_design.md` | `src/arduino/` y `HolaMundoIBeacon/` |
| `android_design.md` | `src/android/` y `BTLEAlumnos2021hechoapp2/app/src/main/java/` |
| `web_rest_design.md` | `src/communication/mediciones.php` |
| `web_logic_design.md` | `src/business_logic/mediciones.php` |
| `communication_design.md` | `src/communication/mediciones.php` |
| `business_logic_design.md` | `src/business_logic/mediciones.php` |
| `web_rest_client_design.md` | `src/communication/PeticionarioREST.js` |
| `frontend_business_logic_design.md` | `src/frontend_business_logic/LogicaFake.js` |
| `web_ux_design.md` | `src/gui/Aplicacion.html`, `Aplicacion.js` |
| `database_design.md` | `src/database/Estructura.sql` |
| `database_connection_design.md` | `src/database/ConexionMediciones.php` |
| `tests_design.md` | `src/tests/` y prueba instrumentada Android en `app/src/androidTest/` |

## Mapa detallado de fuentes

| Área | Archivo/carpeta | Función en la arquitectura |
|---|---|---|
| Arduino | `HolaMundoIBeacon/HolaMundoIBeacon.ino` | Define objetos compartidos, `setup()`, `loop()` y activación manual de pruebas. |
| Arduino | `HolaMundoIBeacon/Medidor.h` | Devuelve los valores simulados CO2 y temperatura. |
| Arduino | `HolaMundoIBeacon/Publicador.h` | Codifica el tipo/contador y publica Major/Minor. |
| Arduino | `HolaMundoIBeacon/test.h` | Emite un beacon centinela, comprueba publicidad y detiene el anuncio. |
| Arduino | `HolaMundoIBeacon/EmisoraBLE.h`, `ServicioEnEmisora.h` | Configuran BLE/Bluefruit y servicios auxiliares. |
| Android | `MainActivity.java` | Pantalla, permisos, servicio y botón de pruebas. |
| Android | `ServicioEscuharBeacons.java` | Escanea BLE y procesa anuncios recibidos. |
| Android | `activity_main.xml` | Incluye el botón de búsqueda del beacon de prueba y el estado del resultado. |
| Android | `TramaIBeacon.java`, `Utilidades.java` | Descomponen la trama y convierten sus campos. |
| Android | `LogicaFake.java` | Valida tipo y valor antes del envío. |
| Android | `PeticionarioREST.java`, `ConfiguracionRest.java` | Cliente HTTP y destino del endpoint. |
| Android | `PeticionarioRESTTest.java` | Prueba instrumentada con MockWebServer. |
| PHP comunicación | `src/communication/mediciones.php` | Adapta la petición a operaciones de negocio. |
| PHP lógica | `src/business_logic/mediciones.php` | Valida, persiste, consulta y normaliza mediciones. |
| PHP datos | `src/database/ConexionMediciones.php`, `Estructura.sql` | Conexión PDO y esquema. |
| Web comunicación | `src/communication/PeticionarioREST.js` | Cliente GET del navegador. |
| Web lógica frontend | `src/frontend_business_logic/LogicaFake.js` | Fachada que entrega datos a la UX. |
| Web UX | `src/gui/Aplicacion.html`, `Aplicacion.js` | Presenta mediciones y estados de pantalla. |
| Pruebas web | `src/tests/` | Comprueba cliente, fachada, UX, lógica, REST y BD. |

## Direcciones de dependencia

```text
Arduino → BLE → Android → HTTP POST → comunicación PHP → lógica → PDO → MySQL
UX web → lógica frontend → cliente HTTP → comunicación PHP → lógica → PDO → MySQL
Botón web → pruebas JavaScript/PHP → lógica y base de pruebas
Botón Android → pruebas manuales de lógica/REST → Logcat
Pulsador Arduino → loop() → test.h → iBeacon PRUEBA=14 → Monitor serie
```

La batería Arduino emite un beacon reservado para pruebas que el receptor Android descarta.
