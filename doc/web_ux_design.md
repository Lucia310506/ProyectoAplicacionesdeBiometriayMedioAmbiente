# Diseño de la interfaz web

## Component Design

### Responsabilidad

Presentar estado de carga, error o éxito y representar las mediciones recibidas en una tabla. La capa visual no construye peticiones HTTP: consume `pedirMediciones()` desde la lógica fake web.

### Fuente de datos REST

La UX llama a `LogicaFake.mostrarMediciones()` y recibe `[ (id: N, tipo: Text, valor: R, fecha: DateTime) ]`. La fachada delega la petición HTTP en `PeticionarioREST.pedirMediciones()`.

### Tipos lógicos

- `Medicion = (id: N, tipo: Text, valor: R, fecha: DateTime)`
- `Mediciones = [ Medicion ]`
- `Estado = (texto: Text, clase: Text)`

### Interfaces lógicas

- `texto: Text, clase: Text --> mostrarEstado() -->`
- `mediciones: Mediciones --> dibujarLista() -->`
- `--> actualizarMediciones() -->`
- `--> iniciarAplicacion() -->`

### Diseño global de la interfaz

`Aplicacion.html` contiene la estructura y los controles; `Aplicacion.js` coordina el estado y genera filas; el CSS aplica presentación. La página inicia una consulta al cargar y repite la consulta cada cinco segundos. El botón de pruebas está asociado al ejecutor separado `pruebas_consola.js`.

```text
┌────────────────────────── Aplicacion.html ───────────────────────────┐
│ estado de consulta: #estado                                          │
│ tabla: #cuerpo-mediciones                                             │
│ botón: #boton-tests                                                   │
└───────────────────────────────┬──────────────────────────────────────┘
                                │ DOM
                                ▼
┌──────────────────────────── Aplicacion.js ───────────────────────────┐
│ iniciarAplicacion() → actualizarMediciones()                          │
│ mostrarEstado(texto, clase)   dibujarLista(mediciones)                 │
└───────────────────────────────┬──────────────────────────────────────┘
                                │ pedirMediciones()
                                ▼
                       PeticionarioREST.js → GET /mediciones
```

### Diseño de cada función

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `mostrarEstado(texto, clase): void` | `texto: Text, clase: Text --> mostrarEstado() -->` | Actualiza el texto y la clase CSS del indicador si existe en el DOM. |
| `dibujarLista(mediciones): void` | `mediciones: Medicion[] --> dibujarLista() -->` | Ordena por fecha, conserva las diez más recientes, elimina las filas viejas y crea celdas con `textContent`. |
| `actualizarMediciones(): Promise<void>` | `--> actualizarMediciones() -->` | Muestra carga, solicita GET, dibuja resultados y muestra éxito o el error recibido. |
| `iniciarAplicacion(): void` | `--> iniciarAplicacion() -->` | Ejecuta la primera consulta y programa consultas repetidas cada cinco segundos. |

### Estados visibles y datos

| Situación | Clase CSS | Comportamiento |
|---|---|---|
| Petición en curso | `carga` | Muestra “Cargando mediciones...”. |
| Consulta completada | `ok` | Indica el número de mediciones o que no hay datos. |
| Fallo de red/servidor | `error` | Muestra el mensaje de error de la petición. |

La lista presenta tipo, valor y fecha en las celdas. El identificador se usa como dato del modelo, pero no se dibuja como columna. Para evitar interpretar valores del servidor como HTML, las celdas se rellenan mediante `textContent`.

### Orden temporal y actualización

La API entrega filas de fecha más reciente a más antigua; la UX hace una copia, la ordena de más antigua a más reciente para facilitar el recorte y toma las últimas diez. No modifica el arreglo original recibido. Tras cada consulta periódica, reemplaza las filas anteriores con las más recientes.

El refresco cada cinco segundos se inicia junto con la primera petición. Si una respuesta falla, se muestra el error, pero el intervalo programado continúa y vuelve a intentar en el ciclo siguiente.

### Dependencias y límites

- Depende de `pedirMediciones()` y de que el HTML incluya los identificadores DOM esperados.
- La tabla muestra un máximo de diez filas; la API devuelve la lista completa.
- El intervalo de cinco segundos se configura directamente en `iniciarAplicacion()`.
- El botón de pruebas ejecuta la batería de consola, no se inicia al cargar la aplicación.

## Design Clarifications

- La descripción y verificación visual de pantallas GUI quedan fuera de la auditoría del agente.

## General Rules

- **Programming Language:** JavaScript y HTML para la interfaz; CSS para estilos.
- **Function/Method Headers:** Cada cabecera debe incluir el diseño lógico en un bloque de comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro y autoexplicativo; comentarios inline mínimos.
- **Automated Testing:** Probar renderizado inicial, estado de carga, estado de error y representación de los datos recibidos por GET.

