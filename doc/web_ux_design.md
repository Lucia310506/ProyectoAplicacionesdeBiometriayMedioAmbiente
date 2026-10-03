# Diseño de la interfaz web

## Component Design

### Responsabilidad

Presentar estado de carga, error o éxito y representar las mediciones recibidas en un gráfico. La capa visual no realiza solicitudes HTTP: consume la interfaz `pedirMediciones`.

### Fuente de datos REST

La UX utiliza la lógica fake web y la ruta `GET /mediciones`; `pedirMediciones()` delega en `mostrarMediciones()` y recibe `[ (id: N, tipo: Text, valor: R, fecha: DateTime) ]`.

### Tipos lógicos

- `Medicion = (id: N, tipo: Text, valor: R, fecha: DateTime)`
- `Mediciones = [ Medicion ]`
- `Estado = (texto: Text, clase: Text)`

### Interfaces lógicas

- `texto: Text, clase: Text --> mostrarEstado() --> Nulo`
- `mediciones: Mediciones --> dibujarGrafico() --> Nulo`
- `--> actualizarMediciones() --> Nulo`
- `--> iniciarAplicacion() --> Nulo`

## Design Clarifications

- La descripción y verificación visual de pantallas GUI quedan fuera de la auditoría del agente.

## General Rules

- **Programming Language:** JavaScript y HTML para la interfaz; CSS para estilos.
- **Function/Method Headers:** Cada cabecera debe incluir el diseño lógico en un bloque de comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro y autoexplicativo; comentarios inline mínimos.
- **Automated Testing:** Probar renderizado inicial, estado de carga, estado de error y representación de los datos recibidos por GET.

