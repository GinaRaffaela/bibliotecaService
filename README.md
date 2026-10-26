# Biblioteca Service

Microservicio REST con Java 17, Spring Boot, Spring Data JPA y Oracle Cloud.
La entidad `Libro` contiene `id`, `titulo`, `autor`, `anioPublicacion` y `genero`.

## Requisitos

- JDK 17 o superior y Maven.
- Una base de datos Oracle Cloud accesible desde el equipo que ejecuta el servicio.
- Un usuario de aplicacion con permisos para crear su tabla y secuencia y operar sobre ellas.

## Conexion a Oracle Cloud

La aplicacion lee la conexion mediante variables de entorno y no almacena credenciales
en el repositorio. Para Oracle Autonomous Database con TLS sin wallet:

1. En la consola de Oracle Cloud, habilita conexiones TLS sin exigir mTLS, si las
   politicas de tu entorno lo permiten. Si el entorno exige mTLS, utiliza un wallet
   y configura el cliente JDBC segun las instrucciones de Oracle; el ejemplo siguiente
   corresponde exclusivamente a TLS sin wallet.
2. Autoriza la IP de salida del servicio en las reglas de acceso de la base de datos.
   Para un endpoint privado, el servicio debe tener conectividad con su red privada.
3. En la seccion de conexiones de la base de datos, selecciona TLS y copia el descriptor
   JDBC proporcionado por Oracle. Usa el host, puerto y nombre de servicio reales;
   no desactives la validacion del certificado del servidor.
4. Conectate con el usuario de aplicacion usando SQLcl o SQL Developer y ejecuta una
   sola vez `src/main/resources/db/schema.sql`. Este script crea la tabla `libros`
   y la secuencia `libros_seq`; no debe ejecutarse de nuevo sobre un esquema preparado.
5. Define las variables en la misma terminal PowerShell desde la que iniciaras Maven:

```powershell
$env:ORACLE_JDBC_URL = 'jdbc:oracle:thin:@(description=(address=(protocol=tcps)(port=1521)(host=HOST_ORACLE))(connect_data=(service_name=SERVICIO_ORACLE))(security=(ssl_server_dn_match=yes)))'
$env:ORACLE_USERNAME = 'USUARIO_APLICACION'
$securePassword = Read-Host 'Password de Oracle' -AsSecureString
$env:ORACLE_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
mvn spring-boot:run
```

Sustituye la URL de ejemplo por la cadena TLS de tu instancia. El puerto tambien debe
coincidir con el descriptor de Oracle. En despliegues, inyecta las credenciales desde
el gestor de secretos de tu plataforma. No uses el usuario administrador para la API.

Hibernate valida el esquema al arrancar (`ddl-auto: validate`), sin crear ni modificar
tablas automaticamente. El puerto HTTP predeterminado es 8080 y se puede cambiar con
la variable `PORT`.

## API REST

Base local: `http://localhost:8080/api/libros`.

| Metodo | Ruta | Resultado |
| --- | --- | --- |
| POST | `/api/libros` | Crea un libro; devuelve 201, el libro y la cabecera Location. |
| GET | `/api/libros/{id}` | Devuelve el libro con estado 200. |
| PUT | `/api/libros/{id}` | Reemplaza los datos del libro y devuelve 200. |
| DELETE | `/api/libros/{id}` | Elimina el libro y devuelve 204 sin cuerpo. |

POST y PUT reciben JSON con todos los campos siguientes. El ID se genera mediante
la secuencia de Oracle y no se envia en el cuerpo:

```json
{
  "titulo": "Don Quijote",
  "autor": "Miguel de Cervantes",
  "anioPublicacion": 1605,
  "genero": "Novela"
}
```

Ejemplo de ciclo completo desde otra terminal PowerShell:

```powershell
$base = 'http://localhost:8080/api/libros'
$datos = @{
    titulo = 'Don Quijote'
    autor = 'Miguel de Cervantes'
    anioPublicacion = 1605
    genero = 'Novela'
}
$libro = Invoke-RestMethod -Method Post -Uri $base -ContentType 'application/json' -Body ($datos | ConvertTo-Json)
Invoke-RestMethod -Method Get -Uri "$base/$($libro.id)"
$datos.genero = 'Novela clasica'
Invoke-RestMethod -Method Put -Uri "$base/$($libro.id)" -ContentType 'application/json' -Body ($datos | ConvertTo-Json)
Invoke-RestMethod -Method Delete -Uri "$base/$($libro.id)"
```

Un ID inexistente devuelve 404. Los campos ausentes o invalidos y los IDs no positivos
devuelven 400. El titulo admite hasta 200 caracteres, el autor hasta 150 y el genero
hasta 100; ninguno puede estar en blanco. El anio debe estar entre 1 y 9999.

## Pruebas

```powershell
mvn test
```

Las pruebas de integracion utilizan MockMvc y H2 con el perfil `test`: verifican el
ciclo CRUD, la persistencia de las actualizaciones, los errores 404 y las validaciones.
No necesitan credenciales ni una conexion a Oracle y no certifican la conectividad
con Oracle Cloud. Esa conexion debe verificarse arrancando el servicio con el esquema
y las variables de entorno de la instancia real y ejecutando el ciclo HTTP anterior.

La API no incluye autenticacion. Antes de exponerla publicamente, protege el acceso
con autenticacion y autorizacion y utiliza HTTPS en el punto de entrada.