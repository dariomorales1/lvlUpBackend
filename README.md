🧱 LevelUP Backend – Plataforma de Microservicios

Sistema backend completo para la plataforma LevelUP Gamer, implementado bajo una arquitectura de microservicios desacoplados, con autenticación, catálogo, usuarios, carritos y órdenes, todo orquestado a través de un API Gateway.
Proyecto académico profesional, diseñado para demostrar dominio avanzado en arquitectura distribuida, seguridad, Spring Boot 3, WebFlux, Swagger, JWT, Firebase Auth y comunicación entre servicios.

👥 Integrantes

FullStack Developer	Felipe Ulloa
FullStack Developer	Darío Morales


🏗️ Arquitectura del Backend

🧩 Microservicios incluidos
Servicio	Puerto	Descripción
API Gateway	8080	Entrada única; enruta a todos los servicios
Auth Service	8081	Login, JWT, refresh, manejo de sesiones
User Service	8082	Usuarios, direcciones, perfiles, avatares
Product Service	8083	Productos, especificaciones, reseñas, imágenes (Supabase)
Cart Service	8084	Carritos autenticados y guest
Order Service	8085	Órdenes, puntos, resumen del usuario


🗺️ Diagrama general de arquitectura

┌──────────────────────────┐
│      Frontend (React)     │
│  App Móvil (Kotlin)       │
└─────────────┬────────────┘
│ HTTP
▼
┌─────────────────────┐
│     API Gateway     │ 8080
└─────────┬──────────┘
┌────────────────────────┼─────────────────────────┐
▼                        ▼                         ▼
┌────────────┐          ┌─────────────┐            ┌──────────────┐
│ AuthSvc    │ 8081     │ UserSvc     │ 8082       │ ProductSvc   │ 8083
└────────────┘          └─────────────┘            └──────────────┘
│                        │                         │
▼                        ▼                         ▼
┌────────────┐          ┌─────────────┐            ┌──────────────┐
│ CartSvc    │ 8084     │ OrderSvc    │ 8085       │  PostgreSQL   │
└────────────┘          └─────────────┘            └──────────────┘

🏗️ Tecnologías Principales
🟦 Backend & Frameworks

Spring Web MVC
Spring WebFlux (Gateway & servicios reactivos)
Spring Security (JWT)
Spring Data JPA

🔐 Autenticación

Firebase Admin SDK
JWT (HMAC256)
Refresh tokens persistidos

🗄 Base de Datos

PostgreSQL

JPA / Hibernate

📦 Integraciones

Supabase Storage (imágenes de productos y usuarios)
WebClient para comunicación entre microservicios

📘 Documentación

Springdoc OpenAPI 3
Swagger UI por microservicio


🧩 Descripción de cada microservicio
1️⃣ API Gateway (8080)

Centraliza todas las peticiones y las enruta a los microservicios internos.
Incluye CORS global, rutas, documentación pública y servidor reactivo WebFlux.

Rutas principales
Ruta	Redirección
/auth/**	Auth Service
/users/**	User Service
/products/**	Product Service
/carts/**	Cart Service
/orders/**	Order Service
2️⃣ Auth Service (8081)

Gestiona la autenticación y generación de tokens.

Funcionalidades

Login con Firebase
Creación automática de usuarios en BD
JWT Access + Refresh Tokens
Refresh automático
Logout

Eliminación de usuario en Firebase

Endpoints principales
Método	Endpoint	Descripción
POST	/auth/login	Login vía Firebase ID Token
POST	/auth/refresh	Genera nuevo access token
POST	/auth/logout	Revoca refresh token
DELETE	/auth/admin/delete-user	Elimina usuario de Firebase
3️⃣ User Service (8082)

Gestiona usuarios, datos personales y direcciones.

Funcionalidades
Perfil de usuario
CRUD de direcciones
Manejo de avatar en Supabase

Endpoints
Método	Endpoint
GET	/users/me
GET	/users/{id}
PUT	/users/{id}
POST	/users/public/register
GET	/users/me/direcciones
POST	/users/me/direcciones
PUT	/users/me/direcciones/{id}
DELETE	/users/me/direcciones/{id}
4️⃣ Product Service (8083)

Encargado del catálogo completo de productos.

Funcionalidades
CRUD de productos
Especificaciones
Reseñas
Subida de imágenes a Supabase
Filtros y búsqueda

Endpoints
Método	Endpoint
GET	/products/
GET	/products/{id}
POST	/products
PUT	/products/{id}
DELETE	/products/{id}
GET	/products/{id}/resenas
5️⃣ Cart Service (8084)

Carritos persistentes para usuarios autenticados e invitados (guest).

Funcionalidades
CRUD de items
Carrito guest con sessionId
Migración guest → usuario
Cálculo automático de totales

Endpoints
Método	Endpoint
GET	/carts/user/{userId}
POST	/carts/user/{userId}/items
PUT	/carts/user/{userId}/items/{productId}
DELETE	/carts/user/{userId}/items/{productId}
DELETE	/carts/user/{userId}/clear
GET	/carts/guest/{sessionId}
POST	/carts/guest/{sessionId}/items
6️⃣ Order Service (8085)

Procesamiento de órdenes, puntos y top de usuarios.

Funcionalidades
Creación de órdenes
Asignación y acumulación de puntos
Resumen de órdenes por usuario
Ranking Top 5 usuarios con más puntos

Endpoints

Método	Endpoint
POST	/orders/user/{id}
GET	/orders/user/{id}
GET	/orders/user/{id}/points

⚙️ Requisitos previos
Java 17
Maven 3.9+
PostgreSQL
Archivo de credenciales Firebase (service-account.json)
Acceso a Supabase (opcional para imágenes)

▶️ Cómo ejecutar cada microservicio
1️⃣ Clonar el repositorio
git clone <repo-url>
cd LevelUpBackend

2️⃣ Configurar variables

Cada microservicio puede requerir:

JWT_SECRET=
FIREBASE_SERVICE_ACCOUNT=
SUPABASE_URL=
SUPABASE_SERVICE_KEY=

3️⃣ Ejecutar cada servicio

Ejemplo para Auth:
cd authService
mvn spring-boot:run


Repetir para:
userService, productService, cartService, orderService, apigateway

🧪 Testing

Cada módulo incluye pruebas básicas:
src/test/java/


Tecnologías:

JUnit 5
SpringBootTest


🎯 Objetivo del proyecto

Este backend demuestra:

Arquitectura real de microservicios
Comunicación interna segura y desacoplada
Gestión profesional de usuarios, productos, carritos y órdenes
Autenticación robusta con Firebase + JWT
Documentación completa con Swagger
Integración con almacenamiento externo (Supabase)

## Despliegue con Docker Compose

### Requisitos
- Docker y Docker Compose instalados
- Archivo `firebase/service-account.json` (credenciales Firebase Admin SDK)

### Pasos

1. **Copiar variables de entorno**
   ```bash
   cp .env.example .env
   ```
   Editar `.env` con los valores reales (DB_HOST, DB_PASSWORD, JWT_SECRET, SUPABASE_URL, etc.)

2. **Verificar archivo Firebase**
   ```bash
   ls firebase/service-account.json
   ```

3. **Construir y levantar**
   ```bash
   docker-compose up -d --build
   ```

4. **Verificar estado**
   ```bash
   docker-compose ps
   ```

5. **Acceder a la API**
   - Gateway: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

6. **Ver logs**
   ```bash
   docker-compose logs -f api-gateway
   docker-compose logs -f auth-service
   ```

7. **Detener servicios**
   ```bash
   docker-compose down
   ```

### Arquitectura Docker

| Contenedor       | Puerto interno | Expuesto al host |
|------------------|---------------|-------------------|
| api-gateway      | 8080          | Si (8080)         |
| auth-service     | 8081          | No                |
| user-service     | 8082          | No                |
| product-service  | 8083          | No                |
| cart-service     | 8084          | No                |
| order-service    | 8085          | No                |

Solo el API Gateway expone puerto al host. Los demas servicios se comunican internamente via la red Docker `lvlup-network`.

---

Proyecto academico para la asignatura ISY1101 - Introduccion a Herramientas DevOps, Duoc UC.

Licencia: uso exclusivamente educativo.