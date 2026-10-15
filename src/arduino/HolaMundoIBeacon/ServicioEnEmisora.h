// -*- mode: c++ -*-
/*
 * Fichero: ServicioEnEmisora.h
 * Autor: LucÃƒÆ’Ã‚Â­a DÃƒÆ’Ã‚Â­az Murcia
 * DescripciÃƒÆ’Ã‚Â³n: Controla el servicio atribuyendole caracterÃƒÆ’Ã‚Â­sticas.
 * Fecha: 2026-09-25
 * Copyright (c) 2026 LucÃƒÆ’Ã‚Â­a DÃƒÆ’Ã‚Â­az Murcia
 * AportaciÃƒÆ’Ã‚Â³n: crear el archivo y hacerlo funcional
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
// ----------------------------------------------------
// --------------------
// pString: Texto, pUint: [N], tamMax: Z --> stringAUint8AlReves() --> [N]
// Copia el UUID textual en orden inverso y limita la longitud al buffer.
// --------------------
uint8_t * stringAUint8AlReves( const char * pString, uint8_t * pUint, int tamMax ) {

	int longitudString =  strlen( pString );
	int longitudCopiar = ( longitudString > tamMax ? tamMax : longitudString );
	// copio nombreServicio -> uuidServicio pero al revÃƒÆ’Ã‚Â©s
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
	uint8_t uuidCaracteristica[16] = { // el uuid se copia aquÃƒÆ’Ã‚Â­ (al revÃƒÆ’Ã‚Â©s) a partir de un string-c
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
// Construye un UUID de caracterÃƒÆ’Ã‚Â­stica a partir de su nombre.
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
// Construye la caracterÃƒÆ’Ã‚Â­stica y configura su contrato GATT.
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
// Configura los permisos de acceso de la caracterÃƒÆ’Ã‚Â­stica.
// --------------------
	void asignarPermisos( SecureMode_t  permisoRead, SecureMode_t  permisoWrite ) {
	  // no puedo escribir AUN si el constructor llama a esto: Serial.println( "laCaracteristica.setPermission( permisoRead, permisoWrite ); " );
	  (*this).laCaracteristica.setPermission( permisoRead, permisoWrite );
	} // ()

	// .........................................................
	// .........................................................
// --------------------
// tam: N --> asignarTamanyoDatos() -->
// Limita el tamaÃƒÆ’Ã‚Â±o mÃƒÆ’Ã‚Â¡ximo de la caracteristica .
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
// Aplica de forma conjunta la configuraciÃƒÆ’Ã‚Â³n de la caracterÃƒÆ’Ã‚Â­stica.
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
// Escribe el valor actual en la caracterÃƒÆ’Ã‚Â­stica.
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

	// .........................................................
	// .........................................................
// --------------------
// cb: CallbackCaracteristicaEscrita --> instalarCallbackCaracteristicaEscrita() -->
// Asocia la funciÃƒÆ’Ã‚Â³n que recibirÃƒÆ’Ã‚Â¡ las escrituras GATT.
// --------------------
	void instalarCallbackCaracteristicaEscrita( CallbackCaracteristicaEscrita cb ) {
	  (*this).laCaracteristica.setWriteCallback( cb );
	} // ()

	// .........................................................
	// .........................................................
// --------------------
// activar() -->
// Registra la caracterÃƒÆ’Ã‚Â­stica en la pila BLE.
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
  
  uint8_t uuidServicio[16] = { // el uuid se copia aquÃƒÆ’Ã‚Â­ (al revÃƒÆ’Ã‚Â©s) a partir de un string-c
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
  // .........................................................
// --------------------
// car: Caracteristica --> anyadirCaracteristica() -->
// AÃƒÆ’Ã‚Â±ade la caracterÃƒÆ’Ã‚Â­stica para activarla junto al servicio.
// --------------------
  void anyadirCaracteristica( Caracteristica & car ) {
	(*this).lasCaracteristicas.push_back( & car );
  } // ()

  // .........................................................
  // .........................................................
// --------------------
// --> activarServicio() 
// Activa el servicio GATT y las caracterÃƒÆ’Ã‚Â­sticas registradas.
// --------------------
  void activarServicio( ) {
	// entiendo que al llegar aquÃƒÆ’Ã‚Â­ ya ha sido configurado
	// todo: caracterÃƒÆ’Ã‚Â­sticas y servicio

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
	// "conversiÃƒÆ’Ã‚Â³n de tipo": si pongo esta clase en un sitio donde necesitan un BLEService
	return elServicio;
  } // ()
	
}; // class

#endif

// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------

