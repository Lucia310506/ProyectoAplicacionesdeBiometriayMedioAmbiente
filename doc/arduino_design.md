# Diseño del firmware BLE

## Component Design

### Responsabilidad

El firmware lee temperatura y CO2 simulados, representa cada lectura en una trama BLE iBeacon y publica las tramas periódicamente. La emisora, el puerto serie, el LED, la medición y la codificación están encapsulados en el sketch y las clases auxiliares.

### Tipos lógicos

- `Medida = { tipo: Text, valor: Z }`
- `TipoMedida = { CO2, TEMPERATURA }`
- `TipoBeacon = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13, PRUEBA = 14 }`
- `EstadoEmision = { INICIADA, DETENIDA, ERROR }`

### Flujo principal

1. Inicializar puerto serie, LED y emisora BLE.
2. Obtener tipo y valor del medidor.
3. Codificar tipo/contador en Major y valor en Minor.
4. Publicar la trama y esperar al siguiente ciclo.

Al pulsar el botón conectado a D2, `loop()` ejecuta `test.h`: emite un iBeacon con tipo reservado `PRUEBA = 14` y valor centinela `0x5A3C`, comprueba que la publicidad se inicia, la mantiene tres segundos para permitir su escaneo y verifica que se detiene.

### Codificación del anuncio iBeacon

| Campo | Codificación | Interpretación en Android |
|---|---|---|
| UUID | `EPSG-GTI-PROY-3A` codificado en 16 bytes | Identifica el emisor/proyecto. |
| Major | `(tipo << 8) + contador` | Byte alto: código de tipo (`11` CO2, `12` temperatura); byte bajo: contador de muestra. |
| Minor | Valor de CO2 o temperatura | Interpretado como entero de 16 bits con signo por el receptor. |
| RSSI/Tx | Valor de referencia configurado en `Publicador`/`EmisoraBLE` | Metadato de señal; no sustituye al valor de medición. |

El contador es de 8 bits y vuelve a empezar al desbordarse. Android evita duplicados comparando el par tipo/contador más reciente; por ello el contrato debe ser compatible en ambos extremos.

### Interfaces lógicas

- `--> iniciar() --> EstadoEmision`
- `--> obtenerMedida() --> Medida`
- `medida: Medida, contador: N --> publicar() --> B`
- `valor: Z --> codificarValor() --> N`

### Estado y dependencias

El sketch coordina `Medidor`, `Publicador`, `EmisoraBLE`, `PuertoSerie` y `LED`. El publicador transforma las medidas en campos del iBeacon; la emisora configura y transmite el anuncio BLE.

### Diseño global de clases

Convención del diagrama de clase: los campos privados se dibujan completamente dentro del contorno. Las funciones públicas salen por una abertura del borde y quedan fuera de la caja; no se dibuja el borde cerrándose sobre una función pública. Las tablas siguientes recogen las funciones públicas expuestas por cada clase.

| Clase | Datos principales | Funciones y responsabilidad |
|---|---|---|
| `Medidor` | Sin estado persistente. | `Medidor()` construye; `iniciarMedidor()` reserva inicialización del sensor; `medirCO2(): int` devuelve el CO2 simulado (18); `medirTemperatura(): int` devuelve temperatura simulada (8). |
| `Publicador` | UUID del beacon, `EmisoraBLE`, RSSI y enumeración `MedicionesID`. | `Publicador()` prepara; `encenderEmisora()` activa radio; `publicarCO2(valor, contador, espera)` codifica tipo/contador en Major y CO2 en Minor; `publicarTemperatura(valor, contador, espera)` realiza lo mismo para temperatura. Ambos anuncios se detienen al acabar el intervalo. |
| `PuertoSerie` | Sin estado de clase adicional. | `PuertoSerie(baudios)` inicia Serial; `esperarDisponible()` espera al monitor; `escribir<T>(mensaje)` escribe cualquier valor compatible con `Serial.print`. |
| `LED` | `numeroLED`, `encendido`. | `LED(numero)` configura y apaga el pin; `encender()` y `apagar()` sincronizan pin/estado; `alternar()` invierte estado; `brillar(tiempo)` ilumina durante el tiempo indicado. |
| `EmisoraBLE` | Configuración Bluefruit, fabricante y servicios/anuncios asociados. | Inicializa la emisora, configura potencia/UUID y publica o detiene anuncios iBeacon. La implementación concreta y sus métodos están en `EmisoraBLE.h`. |
| `ServicioEnEmisora` | Servicio BLE y lista de características asociadas. | Añade características, activa el servicio y convierte a `BLEService&` para interoperar con Bluefruit. |

`Publicador.iniciarBeaconPrueba(contador)` emite el paquete centinela; `estaAnunciando()` comprueba la publicidad y `detenerBeaconPrueba()` la termina. Android descarta el tipo 14, por lo que el paquete no crea una medición en el servidor.

### Activación y casos de la prueba BLE

| Función | Diseño lógico | Qué hace |
|---|---|---|
| `botonPruebasInterrupcion(): void` | `flanco: Interrupcion --> botonPruebasInterrupcion() -->` | Registra la pulsación; no ejecuta la batería dentro de la interrupción. |
| `ejecutarTestsArduino(contador): void` | `contador: N --> ejecutarTestsArduino() -->` | Envía el beacon centinela, confirma el estado de publicidad y muestra el resultado por Serial. |
| `Publicador.iniciarBeaconPrueba(contador): void` | `contador: N --> iniciarBeaconPrueba() -->` | Publica UUID del proyecto, Major con tipo 14/contador y Minor `0x5A3C`. |
| `Publicador.estaAnunciando(): bool` | `--> estaAnunciando() --> B` | Informa si Bluefruit mantiene activo el anuncio. |
| `Publicador.detenerBeaconPrueba(): void` | `--> detenerBeaconPrueba() -->` | Detiene el anuncio después de la ventana de escaneo. |

#### Firmas de BLE y métodos auxiliares

| Clase/función | Diseño lógico | Qué hace |
|---|---|---|
| `EmisoraBLE.encenderEmisora(): void` | `--> encenderEmisora() -->` | Inicializa Bluefruit y activa la radio/emisora. |
| `EmisoraBLE.detenerAnuncio(): void` | `--> detenerAnuncio() -->` | Detiene el anuncio actualmente activo. |
| `EmisoraBLE.estaAnunciando(): bool` | `--> estaAnunciando() --> B` | Informa si hay un anuncio activo. |
| `EmisoraBLE.emitirAnuncioIBeacon(uuid, major, minor, rssi): void` | `uuid: Byte[16], major: N, minor: N, rssi: N --> emitirAnuncioIBeacon() -->` | Configura y publica los campos del anuncio iBeacon. |
| `EmisoraBLE.emitirAnuncioIBeaconLibre(carga): void` | `carga: Text --> emitirAnuncioIBeaconLibre() -->` | Publica una carga libre en el anuncio BLE. |
| `EmisoraBLE.emitirAnuncioIBeaconLibre(carga, tamanyo): void` | `carga: Byte[], tamanyo: N --> emitirAnuncioIBeaconLibre() -->` | Variante que recibe explícitamente el tamaño de la carga. |
| `EmisoraBLE.anyadirServicio(servicio): bool` | `servicio: Servicio --> anyadirServicio() --> B` | Registra un servicio BLE en la emisora. |
| `EmisoraBLE.anyadirServicioConSusCaracteristicas(servicio): bool` | `servicio: Servicio --> anyadirServicioConSusCaracteristicas() --> B` | Registra el servicio junto con sus características. |
| `EmisoraBLE.instalarCallbackConexionEstablecida(callback): void` | `callback: Funcion --> instalarCallbackConexionEstablecida() -->` | Registra el callback al establecerse una conexión BLE. |
| `EmisoraBLE.instalarCallbackConexionTerminada(callback): void` | `callback: Funcion --> instalarCallbackConexionTerminada() -->` | Registra el callback al finalizar una conexión BLE. |
| `EmisoraBLE.getConexion(handle): BLEConnection*` | `handle: N --> getConexion() --> ConexionBLE` | Busca y devuelve el objeto de conexión para el identificador recibido. |
| `ServicioEnEmisora.Caracteristica.asignarPropiedades(props): void` | `props: Byte --> asignarPropiedades() -->` | Define propiedades GATT de la característica. |
| `ServicioEnEmisora.Caracteristica.asignarPermisos(lectura, escritura): void` | `lectura: Permiso, escritura: Permiso --> asignarPermisos() -->` | Configura la seguridad de lectura y escritura. |
| `ServicioEnEmisora.Caracteristica.asignarTamanyoDatos(tam): void` | `tam: Byte --> asignarTamanyoDatos() -->` | Fija el tamaño máximo de datos. |
| `ServicioEnEmisora.Caracteristica.escribirDatos(texto): uint16_t` | `texto: Text --> escribirDatos() --> N` | Escribe datos en la característica. |
| `ServicioEnEmisora.Caracteristica.notificarDatos(texto): uint16_t` | `texto: Text --> notificarDatos() --> N` | Notifica datos a los clientes suscritos. |
| `ServicioEnEmisora.Caracteristica.instalarCallbackCaracteristicaEscrita(callback): void` | `callback: Funcion --> instalarCallbackCaracteristicaEscrita() -->` | Registra la función invocada cuando un cliente escribe. |
| `ServicioEnEmisora.Caracteristica.activar(): void` | `--> activar() -->` | Activa la característica GATT. |
| `ServicioEnEmisora.escribeUUID(): void` | `--> escribeUUID() -->` | Muestra el UUID del servicio por Serial. |
| `ServicioEnEmisora.anyadirCaracteristica(car): void` | `car: Caracteristica --> anyadirCaracteristica() -->` | Agrega una característica al servicio. |
| `ServicioEnEmisora.activarServicio(): void` | `--> activarServicio() -->` | Activa servicio y características registradas. |
| `ServicioEnEmisora.operator BLEService&(): BLEService&` | `servicio: ServicioEnEmisora --> conversion --> BLEService&` | Permite pasar el wrapper a la API de Bluefruit como servicio BLE. |
| `alReves<T>(puntero, n): T*` | `datos: T[], n: N --> alReves() --> T[]` | Invierte el orden de los elementos del arreglo auxiliar. |
| `stringAUint8AlReves(texto, salida, max): uint8_t*` | `texto: Text, salida: Byte[], max: N --> stringAUint8AlReves() --> Byte[]` | Convierte texto a bytes con el orden requerido por UUID. |

## Design Clarifications

- El beacon de prueba usa un tipo reservado que el receptor Android descarta, para evitar guardar una medición ficticia.
- El pulsador solo solicita la prueba; la emisión y las comprobaciones se ejecutan desde `loop()`, nunca dentro de la interrupción.

## General Rules

- **Programming Language:** C++ para Arduino.
- **Function/Method Headers:** Cada cabecera debe incluir el diseño lógico en un bloque de comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro y autoexplicativo; comentarios inline mínimos.
- **Automated Testing:** `test.h` comprueba que el beacon de prueba se inicia y se detiene; se activa con el pulsador D2-GND y se ejecuta desde `loop()`.
