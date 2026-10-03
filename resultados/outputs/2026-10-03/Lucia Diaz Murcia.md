# Informe de auditoría — Lucía Díaz Murcia

- **Repositorio revisado:** `ProyectoAplicacionesdeBiometriayMedioAmbiente` (copia local disponible).
- **Fecha de revisión:** 2026-10-03.
- **Criterios:** `AGENTES_es.pdf` y su transcripción en `Agente-Revisar-Sprint.zip/Agente-Revisar-Sprint/AGENTS.md`, más `context/Software_Engineering_Spec.md` y `context/Database_Design_Spec.md` del ZIP.
- **Alcance:** inspección estática de estructura, diseños disponibles, fuentes y pruebas presentes. No se ejecutaron pruebas ni se revisó funcionamiento en despliegue.
- **Identificación del autor:** no existe `author.md` en la raíz, requisito incumplido. Se usa “Lucía Díaz Murcia” para nombrar este informe porque aparece como autora en los archivos fuente y SQL.

## Requisitos cumplidos

- **Separación de capas en el servidor:** el endpoint `EsqueletoWebAppEnPHPConSesion/src/rest/mediciones.php` delega en `src/logica/mediciones.php`; la persistencia pasa por `src/BBDD/ConexionMediciones.php`.
- **Cliente web separado del endpoint:** `src/logicaFake/PeticionarioREST.js` realiza GET y la UX (`src/ux/Aplicacion.js`) consume `pedirMediciones()`.
- **Separación nominal en Android:** existen `LogicaFake.java` y `PeticionarioREST.java` como archivos/clases distintos.
- **Cabeceras lógicas en fuentes revisadas:** se observan anotaciones de firma lógica y bloques delimitados por guiones en las funciones revisadas, como `guardarMediciones`, `atenderMediciones`, `pedirMediciones` y `enviarMedicion`.
- **Diseño de base de datos en el formato de referencia:** `bbdd/Estructura.sql` implementa `mediciones` con `id`, `tipo`, `valor`, `fecha`, clave primaria, tipos y restricciones coherentes con ese esquema básico.
- **Existencia de pruebas:** hay pruebas de base de datos, REST, cliente REST web y UX en `EsqueletoWebAppEnPHPConSesion/src/tests/`, además de una prueba de ejemplo y `PeticionarioRESTTest.java` de instrumentación Android.

## Requisitos incumplidos

- **`author.md` ausente:** no se puede confirmar el autor mediante el archivo obligatorio de la raíz.
- **`doc/` y diseños ausentes:** no se encontró carpeta `doc/` ni archivos `xxx_design.md`. Por tanto, no existe un diseño por componente contra el que verificar conformidad completa con `Software_Engineering_Spec.md`, ni trazabilidad diseño–implementación.
- **Secciones obligatorias de diseño no verificables:** al no haber archivos de diseño, no se pueden comprobar `Component Design`, `Design Clarifications` (cuando sean necesarias) ni `General Rules`.
- **Reglas generales no verificables:** no hay documentos que declaren el lenguaje destino, exijan comentario lógico delimitado para cada cabecera, indiquen legibilidad mínima de código y requieran pruebas unitarias/de integración para todas las funciones críticas.
- **Directorio `inputs/` ausente:** no se encontró el listado de URLs de repositorios descrito en las instrucciones del agente. La revisión se hizo sobre la copia local indicada por el usuario.
- **Directorio `skills/` del agente sin capacidades aportadas:** se inspeccionó el ZIP y no contiene definiciones de skills ni scripts adicionales. Esto no es un incumplimiento del producto, pero limita automatizaciones que pudieran haberse proporcionado.
- **Cobertura de pruebas críticas no demostrada:** existen pruebas seleccionadas, pero no hay diseño/criterios que acrediten cobertura de todos los métodos críticos. La prueba unitaria local Android encontrada es la prueba de ejemplo `addition_isCorrect`; `PeticionarioRESTTest.java` está en `androidTest`, no en pruebas locales. No se ejecutaron pruebas.

## Hallazgo específico: cliente REST mezclado con lógica fake

En Android, `LogicaFake.guardarMediciones(tipo, valor)` valida el tipo y llama directamente a `PeticionarioREST.enviarMedicion(tipo, valor)`. La clase fake, por tanto, no simula ni mantiene una lógica independiente: su único camino funcional delega en el cliente REST y mezcla el flujo de lógica fake con el envío real. La separación física en dos clases existe, pero la separación de responsabilidades no se cumple en este punto. Esto coincide con el diseño incorrecto señalado por la usuaria.

La ruta web presenta una separación más clara: su `PeticionarioREST.js` está en `src/logicaFake/` por convención del proyecto, pero funciona como cliente HTTP real GET (`fetch`) y no contiene lógica de simulación. Conviene corregir/renombrar esa clasificación o documentar expresamente esa convención en los diseños.

## PDFs proporcionados

El PDF de criterios encontrado dentro del ZIP es `AGENTES_es.pdf`. El archivo `Prompts.pdf` que está en la raíz del proyecto tiene 16 páginas, pero no es el documento llamado “PDF de Agentes” y no se usó como criterio. El informe no atribuye a ese PDF requisitos que no se hayan podido verificar.

## Resultado

**No cumple íntegramente los requisitos del agente de revisión.** Los incumplimientos principales son la ausencia de `author.md`, `doc/` y diseños por componente; además, se confirma el acoplamiento de `LogicaFake` Android al cliente REST. Las observaciones sobre cobertura se limitan a existencia y ubicación de las pruebas, sin ejecución.
