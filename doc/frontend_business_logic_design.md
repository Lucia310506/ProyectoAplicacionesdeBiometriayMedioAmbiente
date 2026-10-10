# Diseño de la lógica de negocio del frontend

## Component Design

### Responsabilidad

La lógica fake web ofrece a la interfaz una operación de dominio para consultar las mediciones. La UX llama a `mostrarMediciones()` y recibe una lista `Medicion[]`; no necesita conocer `fetch`, el código HTTP ni cómo se interpreta el cuerpo de respuesta. El transporte se delega en `PeticionarioREST.js`.

La implementación se encuentra en `EsqueletoWebAppEnPHPConSesion/src/logicaFake/LogicaFake.js`. La ruta forma parte del directorio de fuentes del servidor web ya existente; no se crea un segundo proyecto web ni se duplica la implementación.

### Diseño global del módulo

Es un módulo JavaScript de funciones globales para el navegador, no una clase instanciable ni un repositorio con estado persistente.

```text
UX / Aplicacion.js
       │ mostrarMediciones(): [Medicion]
       ▼
┌──────────────────────── LogicaFake.js ────────────────────────────┐
│ Contrato que ve la UX: Medicion[]                                  │
│ Comprueba que la respuesta sea una lista                           │
└─────────────────────────────┬─────────────────────────────────────┘
                              │ delega el transporte
                              ▼
┌────────────────────── PeticionarioREST.js ─────────────────────────┐
│ pedirMediciones() → GET /mediciones → analiza JSON y estado HTTP   │
└─────────────────────────────┬─────────────────────────────────────┘
                              ▼
                        API PHP /mediciones
```

### Contrato de dominio

```text
Medicion = {
  id: N,
  tipo: Text,            // CO2 o TEMPERATURA
  valor: R,
  fecha: DateTime
}
Mediciones = Medicion[]
```

La función única de este módulo se documenta con el mismo bloque que debe aparecer inmediatamente encima de su implementación:

```text
/*
 * --------------------
 * Entradas: ninguna.
 * Tipo: Medicion = (id: N, tipo: Text, valor: R, fecha: DateTime).
 * --> mostrarMediciones() --> Mediciones = [Medicion]
 * Responsabilidad: obtener las mediciones mediante el cliente REST y entregarlas a la UX.
 * Salida: una lista Mediciones; propaga errores REST o si la respuesta no es una lista.
 * --------------------
 */
```

En JavaScript se declara como `async mostrarMediciones(): Promise<Medicion[]>`; `Promise` expresa la asincronía de implementación. Su contrato lógico sigue siendo `--> mostrarMediciones() --> [Medicion]`. Los errores del cliente REST y las respuestas que no sean listas se propagan para que la UX muestre el fallo.

No hay una operación frontend `guardarMediciones()` porque la interfaz web actual solo consulta y no crea mediciones. La escritura desde Android se valida en `LogicaFake.guardarMediciones(tipo, valor)` y después se envía por REST.

### Paridad con el backend

| Operación | Contrato de negocio backend | Contrato consumido por frontend | Paridad |
|---|---|---|---|
| Leer mediciones | `--> mostrarMediciones() --> [Medicion]` devuelve `id`, `tipo`, `valor`, `fecha`. | `--> mostrarMediciones() --> [Medicion]` presenta el mismo contrato al cliente GUI; la implementación materializa la asincronía con `Promise`. | Identidad de firma lógica y resultado de dominio. |
| Guardar medición | `guardarMediciones(tipo, valor): void`; valida y persiste. | No expuesto por la UX web actual. | No aplica: la interfaz no ofrece alta. |

La fachada puede fallar por conectividad/HTTP y la lógica PHP puede fallar por validación o base de datos. La fachada no altera ni oculta esos errores; la UX es responsable de presentarlos.

### Frontera entre lógica y transporte

- `Aplicacion.js` conoce la operación de dominio `mostrarMediciones()`.
- `LogicaFake.js` define esa interfaz para la UX y comprueba su forma básica de salida.
- `PeticionarioREST.js` es el adaptador HTTP. Su función `pedirMediciones()` es la única que conoce la URL, `fetch`, JSON y `response.ok`.
- El servidor PHP implementa las reglas reales y almacenamiento. La lógica fake del navegador no sustituye a la validación del servidor.

### Pruebas relacionadas

La batería del botón web comprueba que la fachada devuelve una lista con datos válidos y rechaza una respuesta de tipo incorrecto. Por separado, el cliente REST se prueba con `fetch` simulado para éxito GET, error HTTP y fallo de red. La UX se prueba usando una fachada sustituida para comprobar carga, renderizado y error.

## Design Clarifications

- La interfaz actual solo necesita consultar mediciones, por eso el proxy expone solo `mostrarMediciones()`.
- El contrato de dominio no expone objetos `Response`, códigos HTTP, URL ni callbacks del cliente REST.
- La frontera HTTP permanece en `PeticionarioREST.js`; el frontend puede cambiar de transporte sin cambiar las llamadas de la UX.

## General Rules

- **Programming Language:** JavaScript.
- **Function/Method Headers:** Cada función debe llevar inmediatamente encima un bloque de comentario delimitado al principio y al final por líneas `--------------------`. El bloque debe incluir diseño lógico, responsabilidad, entradas y sus tipos (indicar “ninguna” si no hay parámetros) y tipo de salida. Mantener este formato en todas las funciones documentadas.
- **Code Readability:** El módulo contiene únicamente la fachada que consume la interfaz.
- **Automated Testing:** Probar lista correcta, resultado de tipo inválido y errores propagados desde el cliente.
