# Diseño del firmware BLE

## Component Design

### Responsabilidad

El firmware lee temperatura y CO2 simulados, representa cada lectura en una trama BLE iBeacon y publica las tramas periódicamente. La emisora, el puerto serie, el LED, la medición y la codificación están encapsulados en el sketch y las clases auxiliares.

### Tipos lógicos

- `Medida = { tipo: Text, valor: Z }`
- `TipoMedida = { CO2, TEMPERATURA }`
- `EstadoEmision = { INICIADA, DETENIDA, ERROR }`

### Flujo principal

1. Inicializar puerto serie, LED y emisora BLE.
2. Obtener tipo y valor del medidor.
3. Codificar tipo/contador en Major y valor en Minor.
4. Publicar la trama y esperar al siguiente ciclo.

### Interfaces lógicas

- `--> iniciar() --> EstadoEmision`
- `--> obtenerMedida() --> Medida`
- `medida: Medida, contador: N --> publicar() --> B`
- `valor: Z --> codificarValor() --> N`

### Estado y dependencias

El sketch coordina `Medidor`, `Publicador`, `EmisoraBLE`, `PuertoSerie` y `LED`. El publicador transforma las medidas en campos del iBeacon; la emisora configura y transmite el anuncio BLE.

## Design Clarifications

- El medidor actual simula la lectura; no representa un sensor físico calibrado.
- La representación binaria y los límites de Major/Minor deben conservar el protocolo existente del receptor Android.

## General Rules

- **Programming Language:** C++ para Arduino.
- **Function/Method Headers:** Cada cabecera debe incluir el diseño lógico en un bloque de comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro y autoexplicativo; comentarios inline mínimos.
- **Automated Testing:** Las pruebas manuales del medidor están en `test.h`; se activan desde `loop()` al enviar `t` por el monitor serie. No se ejecutan al arrancar. La transmisión BLE se comprueba con la placa.