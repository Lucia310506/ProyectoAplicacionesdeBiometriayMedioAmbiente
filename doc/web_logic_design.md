# Diseño de la lógica de negocio web

## Component Design

### Diseño de datos

`MEDICIONES = [ (dispositivo_id: Text, fecha: DateTime, tipo: Text, valor: R, lugar: Text) ]`

El servidor crea `id` y `fecha`. No se crea una clase `Medicion`; los datos de entrada se pasan directamente como `tipo` y `valor`.

### Diseño lógico

```text
dispositivo_id: Text, tipo: Text, valor: R, lugar: Text --> guardarMediciones() -->

mediciones: [ (dispositivo_id: Text, fecha: DateTime, tipo: Text, valor: R, lugar: Text) ]
             <-- mostrarMediciones() <--
```

### Diseño de clase

Este diagrama conserva el diseño lógico del contrato solicitado. Es conceptual: la implementación PHP es un módulo de funciones globales sin clase instanciable ni estado privado persistente. Los atributos privados quedan completamente dentro. Cada operación pública se coloca sobre la pared vertical (representada por `|`), dejando su espacio abierto; las flechas laterales indican entrada y salida lógica.

```text
                                      -------- LogicaNegocio --------
                                      |
                                      | - mediciones: Medicion[]
                                      |
tipo: Text, valor: R --> | guardarMediciones() -->
                                      |
                                      |
mediciones: Medicion[] <-- | mostrarMediciones() <--
                                      |
                                      --------------------------------
```

La lógica PHP valida los datos y delega el almacenamiento/consulta en la conexión PDO. La implementación acepta los tipos `CO2` y `TEMPERATURA`.

### Ubicación de implementación

`src/business_logic/mediciones.php`.

### Diseño global del módulo `mediciones.php`

Este archivo es un módulo de funciones globales PHP, no una clase instanciable. Valida el contrato de mediciones, ejecuta SQL mediante PDO y adapta las filas devueltas al formato compartido. La conexión se obtiene desde `ConexionMediciones.php`.

```text
┌────────────────────────────── mediciones.php ───────────────────────────────┐
│ Estado propio: ninguno                                                       │
│ guardarMediciones(tipo, valor): void                                         │
│ mostrarMediciones(): Medicion[]                                              │
│ ejecutarPrimeraConsultaValida(PDO, SQL[], parametros?): fila[]               │
│ normalizarFilaMedicion(fila): Medicion                                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `guardarMediciones(string dispositivo_id, string tipo, float valor, string lugar): void` | `dispositivo_id: Text, tipo: Text, valor: R, lugar: Text --> guardarMediciones() -->` | Rechaza dispositivo vacío, tipos distintos de CO2/TEMPERATURA, números no finitos y lugar vacío; inserta con consulta preparada. No devuelve un valor. |
| `mostrarMediciones(): array` | `--> mostrarMediciones() --> Medicion[]` | Consulta las filas ordenadas de más recientes a más antiguas y normaliza cada una. |
| `ejecutarPrimeraConsultaValida(PDO conexion, array consultas, ?array parametros): array` | `bd: BaseDatos, consultas: [Consulta], parametros: [Parametro] --> ejecutarPrimeraConsultaValida() --> [Fila]` | Detalle interno de persistencia; prueba consultas en orden, devuelve filas de lectura o lista vacía al insertar y propaga un fallo si ninguna variante funciona. PDO es detalle de implementación. |
| `normalizarFilaMedicion(array fila): array` | `fila: Dict --> normalizarFilaMedicion() --> Medicion` | Convierte nombres alternativos de columnas y valores a `id` entero, `tipo` texto, `valor` decimal y `fecha` texto. |

Una medición de salida tiene la forma `(dispositivo_id: Text, fecha: DateTime, tipo: Text, valor: R, lugar: Text)`; el alta recibe `dispositivo_id`, `tipo`, `valor` y `lugar`.

### Flujo interno de operaciones

```text
guardarMediciones(tipo, valor)
  ├─ validar tipo ∈ {CO2, TEMPERATURA}
  ├─ validar que valor sea finito
  ├─ resolver entorno y conexión PDO
  └─ ejecutar INSERT preparado; MySQL asigna id/fecha

mostrarMediciones()
  ├─ resolver entorno y conexión PDO
  ├─ ejecutar variante SELECT compatible
  ├─ normalizar cada fila
  └─ devolver lista, ordenada de más reciente a más antigua
```

### Entradas, salidas y errores

- Entrada de escritura: `tipo` texto y `valor` decimal. No hay retorno de negocio; errores se expresan mediante excepciones.
- Tipos permitidos: `CO2` y `TEMPERATURA`, comparados de manera estricta.
- Valor: debe ser finito; `NaN` e infinito se rechazan.
- Lectura: devuelve una lista vacía cuando no hay filas y una lista de mediciones normalizadas cuando sí hay datos.
- Error PDO: se intenta la siguiente variante SQL de compatibilidad; si todas fallan, se lanza el último error PDO.

Las variantes de consultas responden a diferencias históricas de mayúsculas en los nombres de tabla/columnas de despliegues previos. Las consultas siguen usando parámetros enlazados para los valores de inserción.

## Design Clarifications

- Los datos de alta son solo tipo y valor; la base de datos asigna identificador y fecha.
- Las pruebas usan exclusivamente la base de datos de pruebas y vacían/verifican la tabla antes y después de cada caso.

## General Rules

- **Programming Language:** PHP.
- **Function/Method Headers:** Cada función debe tener inmediatamente encima su diseño lógico en un comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro; comentarios adicionales breves solo para decisiones relevantes.
- **Automated Testing:** Probar lógica con base de datos de pruebas: limpiar, insertar, comprobar resultados, limpiar al terminar y verificar tabla vacía.
