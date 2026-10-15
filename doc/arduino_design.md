# DiseÃ±o del firmware BLE

## Component Design

### Responsabilidad

El firmware lee temperatura y CO2 simulados, representa cada lectura en una trama BLE iBeacon y publica las tramas periÃ³dicamente. La emisora, el puerto serie, el LED, la mediciÃ³n y la codificaciÃ³n estÃ¡n encapsulados en el sketch y las clases auxiliares.

### Tipos lÃ³gicos

- `Medida = { tipo: Text, valor: Z }`
- `TipoMedida = { CO2, TEMPERATURA }`
- `TipoBeacon = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13, PRUEBA = 14 }`
- `SensorID = { SENSOR_01 = 1, SENSOR_02 = 2, SENSOR_03 = 3 }`
- `EstadoEmision = { INICIADA, DETENIDA, ERROR }`

### Flujo principal

1. Inicializar puerto serie, LED y emisora BLE.
2. Obtener tipo y valor del medidor.
3. Codificar tipo/contador en Major y valor en Minor.
4. Publicar la trama y esperar al siguiente ciclo.

Al enviar el carÃ¡cter `t` por el monitor serie a 115200 baudios, `loop()` ejecuta `test.h`: emite un iBeacon con tipo reservado `PRUEBA = 14` y valor centinela `0x5A3C`, comprueba que la publicidad se inicia, la mantiene tres segundos para permitir su escaneo y verifica que se detiene.

### CodificaciÃ³n del anuncio iBeacon

| Campo | CodificaciÃ³n | InterpretaciÃ³n en Android |
|---|---|---|
| UUID | `EPSG-GTI-PROY-3A` codificado en 16 bytes | Identifica el emisor/proyecto. |
| Major | `(sensorId << 12) | (tipo << 8) | contador` | Byte alto: cÃ³digo de tipo (`11` CO2, `12` temperatura); byte bajo: contador de muestra. |
| Minor | Valor de CO2 o temperatura | Interpretado como entero de 16 bits con signo por el receptor. |
| RSSI/Tx | Valor de referencia configurado en `Publicador`/`EmisoraBLE` | Metadato de seÃ±al; no sustituye al valor de mediciÃ³n. |

El contador es de 8 bits y vuelve a empezar al desbordarse. Android evita duplicados comparando el par tipo/contador mÃ¡s reciente; por ello el contrato debe ser compatible en ambos extremos.

### Interfaces lÃ³gicas

- `--> iniciar() --> EstadoEmision`
- `--> obtenerMedida() --> Medida`
- `medida: Medida, contador: N --> publicar() --> B`
- `valor: Z --> codificarValor() --> N`

### Estado y dependencias

El sketch coordina `Medidor`, `Publicador`, `EmisoraBLE`, `PuertoSerie` y `LED`. El publicador transforma las medidas en campos del iBeacon; la emisora configura y transmite el anuncio BLE.

### DiseÃ±o global de clases

ConvenciÃ³n del diagrama de clase: los campos privados se dibujan completamente dentro del contorno. Las funciones pÃºblicas salen por una abertura del borde y quedan fuera de la caja; no se dibuja el borde cerrÃ¡ndose sobre una funciÃ³n pÃºblica. Las tablas siguientes recogen las funciones pÃºblicas expuestas por cada clase.

| Clase | Datos principales | Funciones y responsabilidad |
|---|---|---|
| `Medidor` | Sin estado persistente. | `Medidor()` construye; `iniciarMedidor()` reserva inicializaciÃ³n del sensor; `medirCO2(): int` devuelve el CO2 simulado (18); `medirTemperatura(): int` devuelve temperatura simulada (8). |
| `Publicador` | UUID del beacon, `EmisoraBLE`, RSSI y enumeraciÃ³n `MedicionesID`. | `Publicador()` prepara; `encenderEmisora()` activa radio; `publicarCO2(valor, contador, espera)` codifica tipo/contador en Major y CO2 en Minor; `publicarTemperatura(valor, contador, espera)` realiza lo mismo para temperatura. Ambos anuncios se detienen al acabar el intervalo. |
| `PuertoSerie` | Sin estado de clase adicional. | `PuertoSerie(baudios)` inicia Serial; `esperarDisponible()` espera al monitor; `escribir<T>(mensaje)` escribe cualquier valor compatible con `Serial.print`. |
| `EmisoraBLE` | ConfiguraciÃ³n Bluefruit, fabricante y servicios/anuncios asociados. | Inicializa la emisora, configura potencia/UUID y publica o detiene anuncios iBeacon. La implementaciÃ³n concreta y sus mÃ©todos estÃ¡n en `EmisoraBLE.h`. |
| `ServicioEnEmisora` | Servicio BLE y lista de caracterÃ­sticas asociadas. | AÃ±ade caracterÃ­sticas, activa el servicio y convierte a `BLEService&` para interoperar con Bluefruit. |

`Publicador.iniciarBeaconPrueba(contador)` emite el paquete centinela; `estaAnunciando()` comprueba la publicidad y `detenerBeaconPrueba()` la termina. Android descarta el tipo 14, por lo que el paquete no crea una mediciÃ³n en el servidor.

### ActivaciÃ³n y casos de la prueba BLE

| FunciÃ³n | DiseÃ±o lÃ³gico | QuÃ© hace |
|---|---|---|
| `procesarComandoSerial(): void` | `comando: Text --> procesarComandoSerial() -->` | Lee el monitor serie y deja la prueba pendiente cuando recibe `t` o `T`. |
| `ejecutarTestsArduino(contador): void` | `contador: N --> ejecutarTestsArduino() -->` | EnvÃ­a el beacon centinela, confirma el estado de publicidad y muestra el resultado por Serial. |
| `Publicador.iniciarBeaconPrueba(contador): void` | `contador: N --> iniciarBeaconPrueba() -->` | Publica UUID del proyecto, Major con tipo 14/contador y Minor `0x5A3C`. |
| `Publicador.estaAnunciando(): bool` | `--> estaAnunciando() --> B` | Informa si Bluefruit mantiene activo el anuncio. |
| `Publicador.detenerBeaconPrueba(): void` | `--> detenerBeaconPrueba() -->` | Detiene el anuncio despuÃ©s de la ventana de escaneo. |

#### Firmas de BLE y mÃ©todos auxiliares

| Clase/funciÃ³n | DiseÃ±o lÃ³gico | QuÃ© hace |
|---|---|---|
| `EmisoraBLE.encenderEmisora(): void` | `--> encenderEmisora() -->` | Inicializa Bluefruit y activa la radio/emisora. |
| `EmisoraBLE.detenerAnuncio(): void` | `--> detenerAnuncio() -->` | Detiene el anuncio actualmente activo. |
| `EmisoraBLE.estaAnunciando(): bool` | `--> estaAnunciando() --> B` | Informa si hay un anuncio activo. |
| `EmisoraBLE.emitirAnuncioIBeacon(uuid, major, minor, rssi): void` | `uuid: Byte[16], major: N, minor: N, rssi: N --> emitirAnuncioIBeacon() -->` | Configura y publica los campos del anuncio iBeacon. |
| `ServicioEnEmisora.Caracteristica.asignarPropiedades(props): void` | `props: Byte --> asignarPropiedades() -->` | Define propiedades GATT de la caracterÃ­stica. |
| `ServicioEnEmisora.Caracteristica.asignarPermisos(lectura, escritura): void` | `lectura: Permiso, escritura: Permiso --> asignarPermisos() -->` | Configura la seguridad de lectura y escritura. |
| `ServicioEnEmisora.Caracteristica.asignarTamanyoDatos(tam): void` | `tam: Byte --> asignarTamanyoDatos() -->` | Fija el tamaÃ±o mÃ¡ximo de datos. |
| `ServicioEnEmisora.Caracteristica.escribirDatos(texto): uint16_t` | `texto: Text --> escribirDatos() --> N` | Escribe datos en la caracterÃ­stica. |
| `ServicioEnEmisora.Caracteristica.activar(): void` | `--> activar() -->` | Activa la caracterÃ­stica GATT. |
| `ServicioEnEmisora.anyadirCaracteristica(car): void` | `car: Caracteristica --> anyadirCaracteristica() -->` | Agrega una caracterÃ­stica al servicio. |
| `ServicioEnEmisora.activarServicio(): void` | `--> activarServicio() -->` | Activa servicio y caracterÃ­sticas registradas. |
| `ServicioEnEmisora.operator BLEService&(): BLEService&` | `servicio: ServicioEnEmisora --> conversion --> BLEService&` | Permite pasar el wrapper a la API de Bluefruit como servicio BLE. |

## Design Clarifications

- El beacon de prueba usa un tipo reservado que el receptor Android descarta, para evitar guardar una mediciÃ³n ficticia.
- El comando `t` recibido por el monitor serie solo solicita la prueba; la emisiÃ³n y las comprobaciones se ejecutan desde `loop()`.

## General Rules

- **Programming Language:** C++ para Arduino.
- **Function/Method Headers:** Cada cabecera debe incluir el diseÃ±o lÃ³gico en un bloque de comentario delimitado por lÃ­neas `--------------------`.
- **Code Readability:** CÃ³digo claro y autoexplicativo; comentarios inline mÃ­nimos.
- **Automated Testing:** `test.h` comprueba que el beacon de prueba se inicia y se detiene; se activa enviando `t` por el monitor serie y se ejecuta desde `loop()`.
