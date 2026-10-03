# Diseño de conexión a base de datos

## Component Design

### Responsabilidad

Crear y configurar conexiones PDO para los entornos permitidos, manteniendo credenciales fuera de los archivos versionados y usando consultas parametrizadas.

### Tipos lógicos

- `Entorno = { PRODUCCION, PRUEBAS }`
- `Conexion = PDO`

### Interfaces lógicas

- `entorno: Entorno --> obtenerConfiguracion() --> Dict`
- `entorno: Entorno --> conectarBaseDatos() --> Conexion`

## Design Clarifications

La configuración de producción se entrega mediante un archivo local de configuración que no debe publicarse.

## General Rules

- **Programming Language:** PHP para la conexión a base de datos.
- **Function/Method Headers:** Cada cabecera debe incluir el diseño lógico en un bloque de comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro y autoexplicativo; comentarios inline mínimos.
- **Automated Testing:** Verificar conexiones válidas/invalidas, selección de entorno y consultas parametrizadas.
