# Diseño de la lógica de negocio web

## Component Design

### Diseño de datos

`MEDICIONES = [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]`

El servidor crea `id` y `fecha`. No se crea una clase `Medicion`; los datos de entrada se pasan directamente como `tipo` y `valor`.

### Diseño lógico

```text
tipo: Text, valor: R --> guardarMediciones() -->

mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]
             <-- mostrarMediciones() <--
```

### Diseño de clase

```text
                                                          -------- LogicaNegocio --------
                                                          |
                                                          | mediciones: [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]
                                                          |
                       tipo: Text, valor: R --> | guardarMediciones() -->
                                                          |
                                                          |
       mediciones: [ (id: N, tipo: Text,                 |
                      valor: R, fecha: DateTime) ] <-- | mostrarMediciones() <--
                                                          |
                                                          --------------------------------
```

La lógica PHP valida los datos y delega el almacenamiento/consulta en la conexión PDO. La implementación acepta los tipos `CO2` y `TEMPERATURA`.

### Ubicación de implementación

`EsqueletoWebAppEnPHPConSesion/src/logica/mediciones.php`.

## Design Clarifications

- Los datos de alta son solo tipo y valor; la base de datos asigna identificador y fecha.
- Las pruebas usan exclusivamente la base de datos de pruebas y vacían/verifican la tabla antes y después de cada caso.

## General Rules

- **Programming Language:** PHP.
- **Function/Method Headers:** Cada función debe tener inmediatamente encima su diseño lógico en un comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro; comentarios adicionales breves solo para decisiones relevantes.
- **Automated Testing:** Probar lógica con base de datos de pruebas: limpiar, insertar, comprobar resultados, limpiar al terminar y verificar tabla vacía.
