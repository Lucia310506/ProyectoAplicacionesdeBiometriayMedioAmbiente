# Diseño de arquitectura del proyecto

## Component Design

El sistema está formado por firmware BLE, aplicación Android, API REST PHP, lógica de negocio, adaptadores de conexión MySQL, cliente REST web y UX. El firmware publica medidas; Android las recibe y valida antes de enviarlas por HTTP; el servidor valida y persiste; la web consulta y presenta lecturas.

### Contratos principales

- Firmware → Android: trama iBeacon con tipo/contador en Major y valor en Minor.
- Android → API: POST JSON `{ "tipo": Text, "valor": R }`.
- Web → API: GET JSON `[ (id: N, tipo: Text, valor: R, fecha: DateTime) ]`.
- API → lógica → conexión PDO → base MySQL.

## Design Clarifications

La carpeta `EsqueletoWebAppEnPHPConSesion` es la raíz del servidor web dentro de este repositorio; sus subcarpetas `src/rest`, `src/logica`, `src/BBDD` y `src/ux` corresponden a componentes distintos. Android tiene su propia raíz y ciclo de compilación.

## General Rules

- **Programming Language:** C++/Arduino, Java, PHP, JavaScript, HTML/CSS y SQL según el componente.
- **Function/Method Headers:** Cada cabecera debe incluir el diseño lógico en un bloque de comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro y autoexplicativo; comentarios inline mínimos.
- **Automated Testing:** Generar pruebas unitarias/de integración para métodos críticos en cada componente.
