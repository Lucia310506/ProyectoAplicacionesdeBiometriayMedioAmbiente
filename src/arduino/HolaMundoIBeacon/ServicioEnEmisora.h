// -*- mode: c++ -*-
/*
 * Fichero: ServicioEnEmisora.h
 * Autor: Lucía Díaz Murcia
 * Descripción: Controla el servicio atribuyendole características.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 Lucía Díaz Murcia
 */
// ----------------------------------------------------------
// Jordi Bataller i Mascarell
// 2019-07-17
// ----------------------------------------------------------
#ifndef SERVICIO_EMISORA_H_INCLUIDO
#define SERVICIO_EMISORA_H_INCLUIDO

// ----------------------------------------------------
// ----------------------------------------------------
#include <vector>

// ----------------------------------------------------
// alReves() utilidad
// pone al revés el contenido de una array en el mismo array
// ----------------------------------------------------
template< typename T >
// --------------------
// p: T[], n: Z --> alReves() --> T[]
// Invierte los elementos del array en el mismo espacio de memoria.
// --------------------
T *  alReves( T * p, int n ) {
  T aux;

  for( int i=0; i < n/2; i++ ) {
	aux = p[i];
	p[i] = p[n-i-1];
	p[n-i-1] = aux;
  }
  return p;
} // ()

// ----------------------------------------------------
// ----------------------------------------------------
// --------------------
// pString: Texto, pUint: [N], tamMax: Z --> stringAUint8AlReves() --> [N]
// Copia el UUID textual en orden inverso y limita la longitud al buffer.
// --------------------
uint8_t * stringAUint8AlReves( const char * pString, uint8_t * pUint, int tamMax ) {

	int longitudString =  strlen( pString );
	int longitudCopiar = ( longitudString > tamMax ? tamMax : longitudString );
	// copio nombreServicio -> uuidServicio pero al revés
	for( int i=0; i<=longitudCopiar-1; i++ ) {
	  pUint[ tamMax-i-1 ] = pString[ i ];
	} // for

	return pUint;
} // ()

// ----------------------------------------------------------
// ----------------------------------------------------------
class ServicioEnEmisora {

public:


  // --------------------------------------------------------
  // --------------------------------------------------------

  // .........................................................
  // .........................................................
  using CallbackCaracteristicaEscrita = void ( uint16_t conn_handle,
											   BLECharacteristic * chr,
											   uint8_t * data, uint16_t len); 
  // .........................................................
  // .........................................................
  class Caracteristica {
  private:
	uint8_t uuidCaracteristica[16] = { // el uuid se copia aquí (al revés) a partir de un string-c
	  // least signficant byte, el primero
	  '0', '1', '2', '3', 
	  '4', '5', '6', '7', 
	  '8', '9', 'A', 'B', 
	  'C', 'D', 'E', 'F'
	};

	// 
	// 
	// 
	BLECharacteristic laCaracteristica;

  public:

	// .........................................................
	// .........................................................
// --------------------
// nombreCaracteristica_: Texto --> Caracteristica() --> 
// Construye un UUID de característica a partir de su nombre.
// --------------------
	Caracteristica( const char * nombreCaracteristica_ )
	  :
	  laCaracteristica( stringAUint8AlReves( nombreCaracteristica_, &uuidCaracteristica[0], 16 ) )
	{
	  
	} // ()

	// .........................................................
	// .........................................................
// --------------------
// nombreCaracteristica_: Texto, props:N,permisoRead:SecureMode_t,permisoWrite:SecureMode_t --> Caracteristica() -->
// Construye la característica y configura su contrato GATT.
// --------------------
	Caracteristica( const char * nombreCaracteristica_ ,
					uint8_t props,
					SecureMode_t  permisoRead,
					SecureMode_t  permisoWrite, 
					uint8_t tam ) 
	  :
	  Caracteristica( nombreCaracteristica_ ) // llamada al otro constructor
	{
	  (*this).asignarPropiedadesPermisosYTamanyoDatos( props, permisoRead, permisoWrite, tam );
	} // ()

  private:
	// .........................................................
	// CHR_PROPS_WRITE , CHR_PROPS_READ ,  CHR_PROPS_NOTIFY 
  //
	//props: N->asignarPropiedades() ->
	// .........................................................
	void asignarPropiedades ( uint8_t props ) {
	  // no puedo escribir AUN si el constructor llama a esto: Serial.println( " laCaracteristica.setProperties( props ); ");
	  (*this).laCaracteristica.setProperties( props );
	} // ()

	// .........................................................
	// BleSecurityMode::SECMODE_OPEN  , BleSecurityMode::SECMODE_NO_ACCESS
	// .........................................................
// --------------------
// permisoRead: SecureMode_t, permisoWrite: SecureMode_t --> asignarPermisos() -->
// Configura los permisos de acceso de la característica.
// --------------------
	void asignarPermisos( SecureMode_t  permisoRead, SecureMode_t  permisoWrite ) {
	  // no puedo escribir AUN si el constructor llama a esto: Serial.println( "laCaracteristica.setPermission( permisoRead, permisoWrite ); " );
	  (*this).laCaracteristica.setPermission( permisoRead, permisoWrite );
	} // ()

	// .........................................................
	// .........................................................
// --------------------
// tam: N --> asignarTamanyoDatos() -->
// Limita el tamaño máximo del valor GATT.
// --------------------
	void asignarTamanyoDatos( uint8_t tam ) {
	  // no puedo escribir AUN si el constructor llama a esto: Serial.print( " (*this).laCaracteristica.setFixedLen( tam = " );
	  // no puedo escribir AUN si el constructor llama a esto: Serial.println( tam );
	  // (*this).laCaracteristica.setFixedLen( tam );
	  (*this).laCaracteristica.setMaxLen( tam );
	} // ()

  public:
	// .........................................................
	// .........................................................
// --------------------
// props:N,permisoRead:SecureMode_t
//permisoWrite:SecureMode_t tam:N   --> asignarPropiedadesPermisosYTamanyoDatos() -->
// Aplica de forma conjunta la configuración de la característica.
// --------------------
	void asignarPropiedadesPermisosYTamanyoDatos( uint8_t props,
												 SecureMode_t  permisoRead,
												 SecureMode_t  permisoWrite, 
												 uint8_t tam ) {
	  asignarPropiedades( props );
	  asignarPermisos( permisoRead, permisoWrite );
	  asignarTamanyoDatos( tam );
	} // ()
												 

	// .........................................................
	// .........................................................
// --------------------
// str: Texto --> escribirDatos() --> N
// Escribe el valor actual en la característica.
// --------------------
	uint16_t escribirDatos( const char * str ) {
	  // Serial.print( " return (*this).laCaracteristica.write( str  = " );
	  // Serial.println( str );

	  uint16_t r = (*this).laCaracteristica.write( str );

	  // Serial.print( ">>>Escritos " ); Serial.print( r ); Serial.println( " bytes con write() " );

	  return r;
	} // ()

	// .........................................................
	// .........................................................
// --------------------
// str: Texto --> notificarDatos() --> N
// Notifica el valor a los clientes suscritos.
// --------------------
	uint16_t notificarDatos( const char * str ) {
	  
	  uint16_t r = laCaracteristica.notify( &str[0] );

	  return r;
	} //  ()

	// .........................................................
	// .........................................................
// --------------------
// cb: CallbackCaracteristicaEscrita --> instalarCallbackCaracteristicaEscrita() -->
// Asocia la función que recibirá las escrituras GATT.
// --------------------
	void instalarCallbackCaracteristicaEscrita( CallbackCaracteristicaEscrita cb ) {
	  (*this).laCaracteristica.setWriteCallback( cb );
	} // ()

	// .........................................................
	// .........................................................
// --------------------
// activar() -->
// Registra la característica en la pila BLE.
// --------------------
	void activar() {
	  err_t error = (*this).laCaracteristica.begin();
	  Globales::elPuerto.escribir(  " (*this).laCaracteristica.begin(); error = " );
	  Globales::elPuerto.escribir(  error );
	} // ()

  }; // class Caracteristica
  
  // --------------------------------------------------------
  // --------------------------------------------------------
private:
  
  uint8_t uuidServicio[16] = { // el uuid se copia aquí (al revés) a partir de un string-c
	// least signficant byte, el primero
	'0', '1', '2', '3', 
	'4', '5', '6', '7', 
	'8', '9', 'A', 'B', 
	'C', 'D', 'E', 'F'
  };

  //
  //
  //
  BLEService elServicio;

  //
  //
  //
  std::vector< Caracteristica * > lasCaracteristicas;

public:
  
  // .........................................................
  // .........................................................
// --------------------
// nombreServicio_: Texto --> ServicioEnEmisora() -->
// Construye un servicio GATT desde su UUID textual.
// --------------------
  ServicioEnEmisora( const char * nombreServicio_ )
	:
	elServicio( stringAUint8AlReves( nombreServicio_, &uuidServicio[0], 16 ) )
  {
	
  } // ()
  
  // .........................................................
  // .........................................................
// --------------------
// --> escribeUUID() 
// Imprime el UUID del servicio para depuración.
// --------------------
  void escribeUUID() {
	Serial.println ( "**********" );
	for (int i=0; i<= 15; i++) {
	  Serial.print( (char) uuidServicio[i] );
	}
	Serial.println ( "\n**********" );
  } // ()

  // .........................................................
  // .........................................................
// --------------------
// car: Caracteristica --> anyadirCaracteristica() -->
// Añade la característica para activarla junto al servicio.
// --------------------
  void anyadirCaracteristica( Caracteristica & car ) {
	(*this).lasCaracteristicas.push_back( & car );
  } // ()

  // .........................................................
  // .........................................................
// --------------------
// --> activarServicio() 
// Activa el servicio GATT y las características registradas.
// --------------------
  void activarServicio( ) {
	// entiendo que al llegar aquí ya ha sido configurado
	// todo: características y servicio

	err_t error = (*this).elServicio.begin();
	Serial.print( " (*this).elServicio.begin(); error = " );
	Serial.println( error );

	for( auto pCar : (*this).lasCaracteristicas ) {
	  (*pCar).activar();
	} // for

  } // ()

  // .........................................................
  // .........................................................
// --------------------
//  operator BLEService&() --> BLEService&
// Permite pasar el servicio a las funciones de Bluefruit.
// --------------------
  operator BLEService&() {
	// "conversión de tipo": si pongo esta clase en un sitio donde necesitan un BLEService
	return elServicio;
  } // ()
	
}; // class

#endif

// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------

