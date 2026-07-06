# 🎮 El Gato Tic Tac Toe (Arquitectura Cliente-Servidor)

Este proyecto implementa un sistema distribuido desarrollado completamente como una **API REST** utilizando **Java 21** y **Spring Boot 4.x**. La arquitectura se compone de dos microservicios independientes (`gato-server` y `gato-client`), cada uno enlazado a su propia base de datos relacional **MySQL** independiente, desplegados en la nube sobre **Railway**.

---

## 🏗️ Arquitectura

```
JUGADOR 1          ÁRBITRO           JUGADOR 2
gato-client  <-->  gato-server  <-->  gato-client
```

- **gato-server**: árbitro único del sistema. Gestiona la creación de partidas, valida movimientos, detecta victoria/empate/timeout y expone el historial de movimientos.
- **gato-client**: fachada del jugador. Actúa como proxy hacia el server, maneja la identidad local del jugador (registro/login) y hace fallback si el server no responde.

Ambos microservicios corren de forma independiente, cada uno con su propia base de datos MySQL en Railway, comunicándose entre sí vía HTTP mediante red privada.

---

## 🌐 Enlaces en Vivo (Despliegue en Railway)

Para evaluar el correcto funcionamiento de la arquitectura en producción, puede acceder a los servicios a través de los siguientes dominios públicos:

* **Servidor API (gato-server):** [https://gato-server-production.up.railway.app](https://gato-server-production.up.railway.app)
* **Cliente API (gato-client):** [https://gato-tic-tac-toe-copy-production.up.railway.app](https://gato-tic-tac-toe-copy-production.up.railway.app)

> ⚠️ **Nota de seguridad:** Al tratarse de una API REST pura (sin interfaz gráfica), ingresar directamente a estas URLs desde el navegador hará que el sistema responda con un JSON de protección de Spring Security, por ejemplo:
> ```json
> {"error": "UNAUTHORIZED", "message": "Credenciales de jugador requeridas."}
> ```
> Esto es el comportamiento esperado — confirma que la seguridad Basic Auth está activa. Para interactuar realmente con la API, use Postman como se indica abajo.

> 📎 Documentación interactiva (Swagger): agregue `/swagger-ui.html` a cada URL para ver el listado completo de endpoints sin necesidad de autenticarse.

---

## 🚀 Instrucciones para la Evaluación (Postman)

Para interactuar con los endpoints distribuidos (registro de jugadores, creación de partidas, movimientos y persistencia en bases de datos independientes):

1. Importe en Postman los dos archivos incluidos en la raíz del repositorio:
   - **`Gato MS.postman_collection.json`** (la colección con todas las peticiones)
   - **`Gato Local.postman_environment.json`** (el entorno con las variables `serverUrl`, `client1Url`, `client2Url`)
2. Seleccione el entorno **"Gato Local"** en Postman (menú desplegable arriba a la derecha) y complete las variables con las URLs públicas de producción:
   - `serverUrl` → `https://gato-server-production.up.railway.app`
   - `client1Url` → `https://gato-tic-tac-toe-copy-production.up.railway.app`
3. Las peticiones de la colección usan estas variables automáticamente (`{{serverUrl}}`, `{{client1Url}}`), por lo que no es necesario editar cada endpoint manualmente.

### Flujo sugerido de prueba

1. Registrar jugador: `POST {{client1Url}}/api/players`
2. Crear partida: `POST {{client1Url}}/api/game/create`
3. Segundo jugador se une: `POST {{client1Url}}/api/game/{gameId}/join?serverUrl={{serverUrl}}`
4. Realizar movimiento: `POST {{client1Url}}/api/game/{gameId}/move`
5. Ver estado de la partida: `GET {{client1Url}}/api/game/{gameId}/state`
6. Ver historial de movimientos: `GET {{client1Url}}/api/games/{gameId}/history`
7. Verificar disponibilidad del server: `GET {{client1Url}}/api/game/server-health?serverUrl={{serverUrl}}`

---

## 🛠️ Stack Técnico

- Java 21 + Spring Boot 4.x
- MySQL 8 + Flyway (migraciones automáticas)
- Spring Security (Basic Auth)
- Docker + Docker Compose
- JUnit 5 + Mockito
- OpenAPI / Swagger
- Despliegue: Railway (2 servicios + 2 bases de datos MySQL independientes)

---

## 🔒 Seguridad

- **gato-server**: Basic Auth con credenciales de microservicio (tabla `registered_clients`). El cliente se auto-registra al arrancar.
- **gato-client**: Basic Auth con credenciales de jugador (tabla `players`).

---

## ✅ Requerimientos Extendidos Implementados

1. **Health check del server desde el cliente**: `GET /api/game/server-health?serverUrl={url}` — el cliente hace ping al server y retorna disponibilidad y latencia en milisegundos.
2. **Historial de movimientos**: `GET /api/games/{gameId}/history` — el server registra cada movimiento con timestamp, jugador y posición, expuesto al jugador autenticado a través del cliente.

---

## 💻 Ejecución Local (alternativa al despliegue en Railway)

### Requisitos previos
- Java 21, Maven 3.9+, MySQL 8, Docker y Docker Compose

### Con Docker Compose (recomendado)

```bash
docker compose up --build
```

Levanta automáticamente: `gato-server` (8080), `gato-client-1` (8081), `gato-client-2` (8082) y sus respectivas bases de datos MySQL.

### Sin Docker

```bash
# gato-server
cd gato-server
mvn spring-boot:run

# gato-client (jugador 1)
cd gato-client
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --client.id=gato-client-1 --client.base-url=http://localhost:8081"
```

---

## 👤 Autor

Cristopher Meneses — Ingeniería en Informática, Duoc UC
Curso: Fundamentos Fullstack I (DSY1103)
