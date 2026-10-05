# Diseño de base de datos relacional

## Component Design

`MEDICIONES = [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]`

====================================================================================
TABLE: mediciones
DESCRIPTION: Almacena las mediciones ambientales recibidas.

COLUMNS:

+ id | INT | NOT NULL | Auto-Increment
+ tipo | ENUM('CO2', 'TEMPERATURA') | NOT NULL
+ valor | DOUBLE | NOT NULL
+ fecha | DATETIME | NOT NULL | CURRENT_TIMESTAMP

PRIMARY KEY: id

FOREIGN KEYS:

+ Ninguna

CONSTRAINTS:

+ El servidor recibe tipo y valor; genera id y fecha.
+ tipo solo puede ser CO2 o TEMPERATURA.
====================================================================================

## Design Clarifications

No se crea una clase `Medicion`; los datos se manejan directamente con tipo y valor. La estructura SQL existente usa MySQL `ENUM` para restringir el tipo.

## General Rules

- **Programming Language:** SQL (MySQL).
- **Function/Method Headers:** No aplica a declaraciones SQL; toda función de acceso asociada debe documentar su firma lógica en comentario delimitado por líneas `--------------------`.
- **Code Readability:** Definiciones claras y autoexplicativas.
- **Automated Testing:** Verificar tabla y operaciones críticas desde la base de pruebas.
