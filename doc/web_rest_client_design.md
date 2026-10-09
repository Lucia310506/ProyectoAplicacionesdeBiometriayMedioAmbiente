# Diseño de la lógica fake web

## Component Design

### Modelo de datos

`MEDICIONES = [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]`. No se crea clase `Medicion`. Los datos de entrada del alta son tipo y valor; el servidor asigna id y fecha.

### Interfaces lógicas

```text
mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]
             <-- mostrarMediciones() <--

GET /mediciones
mostrarMediciones() --> [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]

mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]
             <-- pedirMediciones() --x
```

`PeticionarioREST.js` solicita `GET /mediciones` y devuelve la lista recibida. La URL REST se configura en un único sitio para alternar entre XAMPP y Plesk.

### Diseño de lógica fake

```text
                                                             -------- lógica negocio --------
                                                             |
tipo: Text, valor: R --> | guardarMediciones() --x
                                                             |
mediciones: [ (id: N, tipo: Text,                          |
               valor: R, fecha: DateTime) ] <-- | mostrarMediciones() --x
                                                             --------------------------------
```

La lógica fake web simula la interfaz de lógica negocio para la UX; el peticionario se comunica con el REST GET según el contrato anterior.

### Ubicación de implementación

`EsqueletoWebAppEnPHPConSesion/src/logicaFake/PeticionarioREST.js` (cliente REST); `src/ux/Aplicacion.js` consume `pedirMediciones()`.

### Diseño global del módulo `PeticionarioREST.js`

El módulo JavaScript no mantiene estado de instancia. Define una constante para la ruta y una operación asíncrona de solo lectura que usa `fetch`.

```text
┌────────────────────── PeticionarioREST.js ──────────────────────┐
│ URL_MEDICIONES = "/mediciones"                                  │
│ pedirMediciones(): Promise<Medicion[]>                           │
└──────────────────────────────────────────────────────────────────┘
```

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `async pedirMediciones(): Promise<Medicion[]>` | `--> pedirMediciones() --x Medicion[]` | Ejecuta GET a `/mediciones`, lee el cuerpo como texto, lo analiza como JSON, informa errores de JSON/HTTP y devuelve la lista. |

La UX consume esta operación; la ruta es relativa al servidor donde se sirve `Aplicacion.html`.

### Contrato de respuesta y errores

La respuesta correcta es un arreglo JSON (incluido `[]` si no hay lecturas). La función primero lee texto y luego analiza JSON para poder dar un mensaje útil si el servidor devuelve HTML, una página de error o un cuerpo vacío. Si el JSON es válido pero `response.ok` es falso, lanza el mensaje `error` del servidor cuando exista; si no, informa que no se pudieron obtener mediciones. Los errores de red propagados por `fetch` también se entregan a la UX como excepciones.

### Secuencia de consulta

```text
actualizarMediciones()
  → pedirMediciones()
  → fetch('/mediciones')
  → response.text()
  → JSON.parse(texto)
  → validar response.ok
  → devuelve Medicion[] o lanza Error
```

El cliente no contiene funcionalidad POST: la escritura se realiza desde Android. Las pruebas reemplazan temporalmente `fetch` para verificar la ruta, JSON, errores HTTP y fallos de red, y restauran la función original al acabar.

## Design Clarifications

- El prompt especifica explícitamente GET para `pedirMediciones()`; no se añade una operación POST al fake web.
- Pruebas del peticionario verifican GET, respuesta JSON y error de red o HTTP.

## General Rules

- **Programming Language:** JavaScript.
- **Function/Method Headers:** Cada función lleva inmediatamente encima su diseño lógico en comentario delimitado por líneas `--------------------`.
- **Code Readability:** Comentarios adicionales breves cuando sean necesarios.
- **Automated Testing:** Verificar GET, datos JSON devueltos y manejo de fallo de red o HTTP.
