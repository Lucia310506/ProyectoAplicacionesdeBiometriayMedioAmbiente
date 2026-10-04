# Diseño de pruebas del servidor

## Component Design

El componente ejecuta pruebas automatizadas aisladas contra la base de pruebas: lógica de base de datos, endpoint REST y cliente/UX JavaScript con dependencias HTTP simuladas. No usa la base de producción.

### Interfaces lógicas

- `condicion: B, mensaje: Text --> comprobar() -->`
- `--> probarBaseDatos() -->`
- `--> probarRest() -->`
- `--> probarPeticionarioRest() -->`
- `--> probarUx() -->`

## Design Clarifications

Las pruebas de Android instrumentadas requieren emulador/dispositivo. La auditoría estática no implica que se hayan ejecutado.

## General Rules

- **Programming Language:** PHP y JavaScript para las pruebas web; Java/JUnit para pruebas Android.
- **Function/Method Headers:** Cada cabecera debe incluir el diseño lógico en un bloque de comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro y autoexplicativo; comentarios inline mínimos.
- **Automated Testing:** Aislar datos de prueba y cubrir flujos críticos y errores esperados.
