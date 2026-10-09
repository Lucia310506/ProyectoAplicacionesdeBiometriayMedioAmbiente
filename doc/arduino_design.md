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

## Design Clarifications

### Diseño global del firmware

`HolaMundoIBeacon.ino` coordina objetos globales de LED, puerto serie, publicador y medidor. La prueba se solicita con un botón físico: la interrupción solo registra la pulsación y `loop()` ejecuta `test.h` en el ciclo principal, fuera de la interrupción.

```text
┌────────────────────────── Firmware global ──────────────────────────────┐
│ Globales: LED elLED, PuertoSerie elPuerto, Publicador elPublicador,      │
│           Medidor elMedidor                                               │
│ PIN_BOTON_TESTS = D2; bandera pruebasSolicitadas                         │
│ botonPruebasInterrupcion()                                                │
│ inicializarPlaquita(); setup(); lucecitas(); loop()                      │
└──────────────────────────────────────────────────────────────────────────┘
          │ loop publica lecturas                     │ botón activa
          ▼                                           ▼
   Medidor → Publicador → EmisoraBLE             test.h / ejecutarTestsArduino()
```

| Función global | Diseño lógico | Qué hace |
|---|---|---|
| `botonPruebasInterrupcion(): void` | `flanco: Interrupcion --> botonPruebasInterrupcion() -->` | Aplica antirrebote y marca que hay una prueba pendiente; no ejecuta pruebas dentro de la interrupción. |
| `inicializarPlaquita(): void` | `--> inicializarPlaquita() -->` | Reserva el punto de inicialización adicional de la placa. |
| `setup(): void` | `--> setup() -->` | Inicializa serie, pulsador/interrupción, emisora BLE y medidor. |
| `lucecitas(): void` | `--> lucecitas() -->` | Ejecuta la secuencia visual del ciclo. |
| `loop(): void` | `--> loop() -->` | Consume la bandera de prueba en el hilo principal, ejecuta pruebas solicitadas y anuncia CO2/temperatura en ciclos. |
| `esperar(long tiempo): void` | `tiempo: N --> esperar() -->` | Espera el intervalo mediante `delay`. |
| `ejecutarTestsArduino(): bool` | `--> ejecutarTestsArduino() --> B` | Comprueba las lecturas simuladas y escribe el resultado por Serial. |

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

### Diseño global de las pruebas `test.h`

```text
┌──────────────────────── test.h ────────────────────────┐
│ ejecutarTestsArduino(): bool                            │
│  ├─ medidorDePrueba.medirCO2() == 18                    │
│  ├─ medidorDePrueba.medirTemperatura() == 8             │
│  └─ imprime casos y resumen en Serial                   │
└────────────────────────────────────────────────────────┘
```

`ejecutarTestsArduino()` crea un `Medidor` de prueba, comprueba ambos valores y devuelve si pasaron. La pulsación D2-GND dispara la solicitud; `loop()` ejecuta la función fuera de la interrupción.

- El medidor actual simula la lectura; no representa un sensor físico calibrado.
- La representación binaria y los límites de Major/Minor deben conservar el protocolo existente del receptor Android.

## General Rules

- **Programming Language:** C++ para Arduino.
- **Function/Method Headers:** Cada cabecera debe incluir el diseño lógico en un bloque de comentario delimitado por líneas `--------------------`.
- **Code Readability:** Código claro y autoexplicativo; comentarios inline mínimos.
- **Automated Testing:** Las pruebas manuales del medidor están en `test.h`; se activan al pulsar un botón conectado entre D2 y GND. No se ejecutan al arrancar.
