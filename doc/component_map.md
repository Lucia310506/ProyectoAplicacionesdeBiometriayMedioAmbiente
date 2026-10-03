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

Los directorios `src/xxx/` requeridos literalmente por la plantilla no se han duplicado: las implementaciones ya viven en las raíces históricas anteriores. Este mapa documenta la equivalencia sin mover código fuente ni romper Gradle, rutas PHP o el sketch Arduino.

