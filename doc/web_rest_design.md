# Diseño del servidor REST

## Component Design

### Modelo compartido

`MEDICIONES = [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]`. No se crea una clase `Medicion`. El cliente envía `tipo` y `valor`; el servidor crea `id` y `fecha`.

### Rutas REST

```text
POST /mediciones
(tipo: Text, valor: R) --> guardarMediciones() -->
JSON de entrada: { "tipo": "CO2", "valor": 500 }
El POST Android devuelve una respuesta HTTP 201 cuando se guarda correctamente; el cliente debe informar errores HTTP.

GET /mediciones
mostrarMediciones() --> [ (id: N, tipo: Text, valor: R, fecha: DateTime) ]
JSON de salida: [
  { "id": 1, "tipo": "CO2", "valor": 500, "fecha": "2026-09-25T10:30:00" },
  { "id": 2, "tipo": "TEMPERATURA", "valor": -19, "fecha": "2026-09-25T10:31:00" }
]
```

### Dependencias y límites

El endpoint delega toda persistencia a la lógica de negocio. La lógica utiliza PDO y consultas preparadas. La configuración separa desarrollo XAMPP, producción Plesk y pruebas; las credenciales reales no forman parte del repositorio.

### Ubicación de implementación

`EsqueletoWebAppEnPHPConSesion/src/rest/mediciones.php`.

## Design Clarifications

- El contrato es `/mediciones`; `.htaccess` puede reescribir la URL hacia el archivo PHP del endpoint.
- Los tests de integración de GET y POST usan solo la base de datos de pruebas y comprueban el ciclo de limpieza.

## General Rules

- **Programming Language:** PHP para el servidor y JSON para el contrato HTTP.
- **Function/Method Headers:** Cada función debe llevar inmediatamente encima su diseño lógico, delimitado por líneas `--------------------`.
- **Code Readability:** Comentarios adicionales breves solo para decisiones importantes.
- **Automated Testing:** Integración de POST y GET; vaciar, preparar datos, comprobar, borrar y verificar tabla vacía.

