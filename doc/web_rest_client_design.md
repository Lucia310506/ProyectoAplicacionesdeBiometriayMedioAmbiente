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

## Design Clarifications

- El prompt especifica explícitamente GET para `pedirMediciones()`; no se añade una operación POST al fake web.
- Pruebas del peticionario verifican GET, respuesta JSON y error de red o HTTP.

## General Rules

- **Programming Language:** JavaScript.
- **Function/Method Headers:** Cada función lleva inmediatamente encima su diseño lógico en comentario delimitado por líneas `--------------------`.
- **Code Readability:** Comentarios adicionales breves cuando sean necesarios.
- **Automated Testing:** Verificar GET, datos JSON devueltos y manejo de fallo de red o HTTP.
