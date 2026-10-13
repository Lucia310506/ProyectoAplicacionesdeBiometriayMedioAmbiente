# Diseño de base de datos relacional

## Component Design

`MEDICIONES = [ (dispositivo_id: Text, fecha: DateTime, tipo: Text, valor: R, lugar: Text) ]`

====================================================================================
TABLE: mediciones
DESCRIPTION: Almacena las mediciones ambientales recibidas.

COLUMNS:

+ dispositivo_id | VARCHAR(64) | NOT NULL
+ fecha | DATETIME(6) | NOT NULL | CURRENT_TIMESTAMP(6)
+ tipo | ENUM('CO2', 'TEMPERATURA') | NOT NULL
+ valor | DOUBLE | NOT NULL
+ lugar | VARCHAR(100) | NOT NULL | Ubicación no disponible

PRIMARY KEY: (dispositivo_id, fecha)

FOREIGN KEYS:

+ Ninguna

CONSTRAINTS:

+ El servidor recibe dispositivo_id, tipo y valor; genera la fecha con microsegundos.
+ tipo solo puede ser CO2 o TEMPERATURA.
+ La combinación dispositivo_id + fecha identifica la medición.
====================================================================================

### Diseño global de la base de datos

La base de datos no define una clase global ni funciones propias: el objeto persistente es la tabla `mediciones`. La lógica PHP accede a ella mediante PDO.

```text
┌────────────────────────────── mediciones ───────────────────────────────┐
│ dispositivo_id: VARCHAR(64), clave primaria compuesta                      │
│ fecha: DATETIME(6), clave primaria compuesta, DEFAULT CURRENT_TIMESTAMP(6)│
│ tipo: ENUM('CO2', 'TEMPERATURA'), obligatorio                             │
│ valor: DOUBLE, obligatorio                                                │
└──────────────────────────────────────────────────────────────────────────┘
Alta: (dispositivo_id, tipo, valor) → INSERT; la BD genera la fecha.
Lectura: SELECT → mediciones ordenadas por fecha y dispositivo_id descendentes.
```

| Operación | Diseño lógico | Qué hace |
|---|---|---|
| Alta | `tipo: Text, valor: R --> INSERT mediciones -->` | Persiste una medición válida. `id` y `fecha` se generan en la base de datos. |
| Lectura | `--> SELECT mediciones --> Medicion[]` | Devuelve id, tipo, valor y fecha de las filas almacenadas. |
| Limpieza de pruebas | `--> DELETE FROM mediciones -->` | Limpia las filas de la base exclusiva de pruebas antes y después de la batería. |

### Esquema y reglas de datos

| Columna | Tipo | Regla | Origen |
|---|---|---|---|
| `id` | `INT` | `NOT NULL`, `AUTO_INCREMENT`, clave primaria | Asignado por MySQL. |
| `tipo` | `ENUM('CO2','TEMPERATURA')` | `NOT NULL`; solo permite los dos tipos del sistema | Recibido en el JSON y validado también por lógica PHP. |
| `valor` | `DOUBLE` | `NOT NULL` | Recibido como número y validado como finito por lógica PHP. |
| `fecha` | `DATETIME` | `NOT NULL`, default `CURRENT_TIMESTAMP` | Asignada por MySQL en la creación de la fila. |

La tabla usa InnoDB y `utf8mb4`. No hay relaciones con otras tablas ni claves foráneas. El índice explícito es la clave primaria compuesta `(dispositivo_id, fecha)`.

### Ciclo de vida de una fila

1. La entrada HTTP aporta `dispositivo_id`, `tipo` y `valor`; no se acepta `fecha` como campo de alta.
2. La validación de negocio limita el conjunto de tipos y rechaza valores no finitos y dispositivos vacíos.
3. El INSERT parametrizado almacena la medición y la BD genera la fecha con microsegundos.
4. El SELECT convierte fecha a formato ISO de fecha/hora y ordena fecha y dispositivo_id en orden descendente.
5. `normalizarFilaMedicion()` entrega al REST una representación estable aunque MySQL devuelva variaciones de mayúsculas en los nombres de columnas.

## Design Clarifications

- No se crea una clase `Medicion`; los datos se manejan directamente con dispositivo, tipo y valor. La estructura SQL existente usa MySQL `ENUM` para restringir el tipo.

## General Rules

- **Programming Language:** SQL (MySQL).
- **Function/Method Headers:** No aplica a declaraciones SQL; toda función de acceso asociada debe documentar su firma lógica en comentario delimitado por líneas `--------------------`.
- **Code Readability:** Definiciones claras y autoexplicativas.
- **Automated Testing:** Verificar tabla y operaciones críticas desde la base de pruebas.
