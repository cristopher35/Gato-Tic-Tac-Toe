# El Gato Distribuido — Fullstack I

Sistema distribuido de Tic-Tac-Toe implementado con dos microservicios Spring Boot.

## Stack técnico

- Java 21 + Spring Boot 4.x
- MySQL 8 + Flyway
- Spring Security (Basic Auth)
- Docker + Docker Compose
- JUnit 5 + Mockito
- OpenAPI / Swagger

## Arquitectura

```
JUGADOR 1          ÁRBITRO           JUGADOR 2
gato-client  <-->  gato-server  <-->  gato-client
:8081               :8080              :8082
```

- **gato-server** (puerto 8080): árbitro único. Gestiona partidas, valida movimientos, detecta victoria/empate/timeout.
- **gato-client** (puertos 8081 / 8082): fachada del jugador. Proxy hacia el server, maneja identidad local, fallback ante fallos.

## Requisitos previos

- Java 21
- Maven 3.9+
- MySQL 8 corriendo localmente
- Docker y Docker Compose (para despliegue containerizado)

## Configuración de base de datos (sin Docker)

```sql
CREATE DATABASE db_gato_server;
CREATE DATABASE db_gato_client;
```

Las tablas se crean automáticamente con Flyway al arrancar.

## Ejecución sin Docker

### gato-server (puerto 8080)

```bash
cd gato-server
mvn spring-boot:run
```

### gato-client — Instancia Jugador 1 (puerto 8081)

```bash
cd gato-client
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --client.id=gato-client-1 --client.base-url=http://localhost:8081"
```

### gato-client — Instancia Jugador 2 (puerto 8082)

```bash
cd gato-client
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --client.id=gato-client-2 --client.base-url=http://localhost:8082 --spring.datasource.url=jdbc:mysql://localhost:3306/db_gato_client_2"
```

## Ejecución con Docker Compose

```bash
docker compose up --build
```

Levanta: gato-server, gato-client-1 (8081), gato-client-2 (8082) y sus bases de datos.

## Flujo de una partida

1. Registrar jugador en el cliente: `POST /api/players`
2. Crear partida: `POST /api/game/create` (con `serverUrl`)
3. Jugador 2 se une: `POST /api/game/{gameId}/join?serverUrl=...`
4. Mover: `POST /api/game/{gameId}/move`
5. Ver estado: `GET /api/game/{gameId}/state`

## Seguridad

- **gato-server**: Basic Auth con credenciales de MS (tabla `registered_clients`). El cliente se auto-registra al arrancar.
- **gato-client**: Basic Auth con credenciales del jugador (tabla `players`).

## Swagger UI

- gato-server: http://localhost:8080/swagger-ui.html
- gato-client: http://localhost:8081/swagger-ui.html

## Requerimientos extendidos

1. **Health check del server desde el cliente**: `GET /api/game/server-health?serverUrl={url}` — el cliente hace ping al server y retorna disponibilidad y latencia.
2. **Historial de movimientos**: `GET /api/games/{gameId}/history` — el server registra cada movimiento con timestamp y el cliente lo expone al jugador autenticado.

## Variables de entorno

| Variable | Descripción | Default |
|---|---|---|
| DB_URL | URL de la base de datos | jdbc:mysql://localhost:3306/db_gato_server |
| DB_USERNAME | Usuario MySQL | root |
| DB_PASSWORD | Contraseña MySQL | — |
| SERVER_PORT | Puerto del servicio | 8080 / 8081 |
| CLIENT_ID | ID del MS Cliente | gato-client-1 |
| CLIENT_SECRET | Secret del MS Cliente | — |
| CLIENT_BASE_URL | URL base del cliente | http://localhost:8081 |
| SERVER_TARGET_URL | URL del server destino | http://localhost:8080 |

## Autor

Cristopher Meneses — Ingeniería en Informática, Duoc UC  
Curso: Fundamentos Fullstack I (FPY-1101)
