# Microsserviços com Spring Boot — Eureka, Gateway, Swagger e Zipkin

Projeto de estudo (Seção 14 — *Using Zipkin Distributed Tracing Server*) que implementa uma arquitetura
de microsserviços com **Spring Boot 4.1.1** e **Spring Cloud**, incluindo descoberta de serviços
(Netflix Eureka), API Gateway (Spring Cloud Gateway), documentação de APIs (Swagger/OpenAPI via SpringDoc)
e **tracing distribuido com Zipkin** (Micrometer Tracing + Brave).

---

## 🏛️ Arquitetura

```
                          ┌─────────────────────┐
                          │    Client / Browser  │
                          └──────────┬───────────┘
                                     │ http://localhost:8765
                                     ▼
        ┌────────────────────────────────────────────────┐
        │                api-gateway (:8765)              │
        │  Spring Cloud Gateway (WebFlux) + Eureka Client │
        │  Rotas: /book-service/**    → lb://book-service │
        │         /exchange-service/** → lb://exchange-service
        └──────────┬──────────────────────────┬──────────┘
                   ▼                          │
     ┌───────────────────────┐   (Feign)      │
     │  book-service (:8100) │────────────────┘
     │  JPA + MySQL + Feign  │
     └──────────┬────────────┘
                ▼
     ┌────────────────────────┐      ┌─────────────────────────┐
     │ exchange-service (:8000)│◄────►│  naming-server (:8761)   │
     │      JPA + MySQL        │      │  Netflix Eureka Server   │
     └──────────┬─────────────┘      └───────────▲─────────────┘
                │                                │ registro
   MySQL exchange-db (:3300)                     │ (todos os serviços)
   MySQL book-db      (:3301)

   todos os serviços ──► zipkin-server (:9411)   (spans via HTTP)
```

---

## 📦 Módulos

| Módulo | Porta | Descrição |
|---|---|---|
| **naming-server** | `8761` | Netflix Eureka Server — registro e descoberta de serviços |
| **api-gateway** | `8765` | Spring Cloud Gateway (WebFlux) — ponto único de entrada, roteamento via `lb://` (load balance com Eureka) |
| **book-service** | `8100` | CRUD de livros (JPA + MySQL). Chama o exchange-service via **OpenFeign** para converter preço/câmbio |
| **exchange-service** | `8000` | Conversão de moedas (JPA + MySQL) |
| **zipkin-server** | `9411` | Servidor de tracing distribuido (imagem `openzipkin/zipkin:3.5.1`) |
| **book-db / exchange-db** | `3301` / `3300` | MySQL 8.0 por serviço (padrão *database per service*) |

---

## 🧰 Tecnologias utilizadas

### Core
- **Java 21**
- **Spring Boot 4.1.1** (Maven, `spring-boot-maven-plugin` com build de imagem *buildpacks*)

### Microsserviços
- **Netflix Eureka** (`spring-cloud-starter-netflix-eureka-server` / `-client`) — service discovery
- **Spring Cloud Gateway Server WebFlux** — API Gateway reativo com rotas dinâmicas (`lb://service-name`)
- **OpenFeign** (`spring-cloud-starter-openfeign`) + `feign-micrometer` — comunicação declarativa book → exchange
- **Spring Boot Actuator** — health checks consumidos pelo Eureka

### Persistência
- **Spring Data JPA**
- **MySQL 8.0** (`mysql-connector-j`)
- **Flyway** (`spring-boot-starter-flyway` + `flyway-mysql`) — migrações em `src/main/resources/db/migration`

### Documentação de APIs — Swagger / OpenAPI
- **SpringDoc OpenAPI 3.2.0**
  - `springdoc-openapi-starter-webmvc-ui` (book-service e exchange-service)
  - `springdoc-openapi-starter-webflux-ui` (api-gateway, agregando os docs dos serviços)

### Observabilidade — Tracing Distribuido
- **Micrometer Tracing** + **Brave** (`micrometer-tracing-bridge-brave`)
- **spring-boot-starter-zipkin** + `zipkin-reporter-brave 3.5.3`
- **Zipkin Server 3.5.1** (`openzipkin/zipkin`) — UI em `http://localhost:9411/zipkin/`

### Infra & Outros
- **Docker** + **Docker Compose** — rede `microservices-app`, volumes MySQL
- **Lombok** e **Spring Boot DevTools**

---

## ▶️ Como executar

### Pré-requisitos
Java 21+ · Maven 3.9+ · Docker + Docker Compose

### Build dos JARs
Em cada módulo (`naming-server`, `api-gateway`, `book-service`, `exchange-service`):

```bash
mvn clean package
```

> O `Dockerfile` de cada serviço copia o `target/*.jar` para a imagem `eclipse-temurin:21-jre`.
> Rode o `mvn package` **antes** do compose e sempre que alterar um `.java` **ou** o
> `application.yaml` — o YAML é empacotado dentro do JAR, então mudar só o YAML exige `--build`.

### Subir a stack
A partir da pasta `naming-server/`:

```bash
docker compose up -d --build
```

Após mudar env/config de um serviço:

```bash
docker compose up -d --force-recreate api-gateway
```

---

## 🌐 URLs úteis

| Serviço | URL |
|---|---|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8765 |
| Zipkin UI | http://localhost:9411/zipkin/ |
| Swagger UI — agregado (via gateway) | http://localhost:8765/swagger-ui/index.html |
| Swagger UI — book-service | http://localhost:8100/swagger-ui/index.html |
| Swagger UI — exchange-service | http://localhost:8000/swagger-ui/index.html |
| OpenAPI docs — book-service | http://localhost:8100/book-service/v3/api-docs |
| OpenAPI docs — exchange-service | http://localhost:8000/exchange-service/v3/api-docs |
| MySQL (book) | localhost:3301 — user `mysql` / `123456`, db `book_service` |
| MySQL (exchange) | localhost:3300 — user `mysql` / `123456`, db `exchange_service` |

> ⚠️ No Windows + Docker Desktop (WSL2), se `http://localhost` travar no PowerShell/`curl`,
> use `http://127.0.0.1` — o proxy do Docker pode não escutar em IPv6 (`::1`).
> No navegador, `localhost` funciona normalmente.

---

## 🔗 Endpoints

### book-service
`GET /book-service/{id}/{currency}` — retorna o livro com o preço convertido para a moeda pedida
(conversão feita pelo exchange-service via Feign).

```bash
curl http://localhost:8765/book-service/1/BRL
```

```json
{
  "id": 1,
  "author": "Michael C. Feathers",
  "title": "Working Effectively with Legacy Code",
  "price": 47.7349,
  "currency": "BRL",
  "environment": "BOOK PORT: 8100 | EXCHANGE PORT: 8000"
}
```

### exchange-service
`GET /exchange-service/{amount}/{from}/{to}`

```bash
curl http://localhost:8000/exchange-service/100/BRL/USD
```

---

## 🔍 Tracing Distribuido com Zipkin

Todos os serviços (gateway, book e exchange) enviam **spans** ao `zipkin-server` via HTTP,
com **100% de amostragem** (`probability = 1.0`). O `traceId` é propagado
`api-gateway → book-service → exchange-service` (inclusive pelos headers do OpenFeign),
formando o trace completo da requisição na UI do Zipkin.

### Propriedades (⚠️ nomes do Spring Boot 4)

No Spring Boot 4 o Zipkin **mudou de prefixo** — confira qual nome sua versão usa:

| Boot 2 / Sleuth (obsoleto) | Boot 3.x (obsoleto no 4) | **Boot 4 (correto)** |
|---|---|---|
| `spring.zipkin.base-url` | `management.zipkin.tracing.endpoint` | `management.tracing.export.zipkin.endpoint` |

Configuração usada (ex.: `api-gateway/src/main/resources/application.yaml`):

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
    export:
      zipkin:
        endpoint: ${MANAGEMENT_TRACING_EXPORT_ZIPKIN_ENDPOINT:http://zipkin-server:9411/api/v2/spans}
```

E no `docker-compose.yml` (override por container):

```yaml
MANAGEMENT_TRACING_EXPORT_ZIPKIN_ENDPOINT: http://zipkin-server:9411/api/v2/spans
MANAGEMENT_TRACING_SAMPLING_PROBABILITY: 1.0
```

> ⚠️ **Pegadinha descoberta na prática**: se a propriedade não for reconhecida (nome antigo),
> o app cai silenciosamente no default `http://localhost:9411/api/v2/spans` — dentro do container
> isso falha com `ConnectException` em todo flush, e o `AsyncReporter` só loga a **primeira** falha.
> No Boot 4 o valor é o **endpoint completo** (`/api/v2/spans` incluso), não a *base URL* como no Boot 2/3.

### Verificando a ingestão via API

```bash
# lista os serviços que enviaram spans
curl http://localhost:9411/api/v2/services

# traces dos últimos 10 min (lookback em milissegundos; opcional: serviceName, limit)
curl "http://localhost:9411/api/v2/traces?serviceName=book-service&lookback=600000&limit=5"
```

> Nota: `GET /api/v2/spans` retorna **400** por design — a ingestão é feita via **POST** `/api/v2/spans`
> (o que os serviços fazem). Para consulta use a UI `/zipkin/` ou a API `/api/v2/traces`.

---

## 🗄️ Banco de dados (Flyway)

Cada serviço possui seu próprio MySQL (*database per service*), inicializado por migrações versionadas:

- `book-service/src/main/resources/db/migration`
  - `V1__Create_Table_Books.sql`
  - `V2__Insert_Data_In_Books.sql`
- `exchange-service/src/main/resources/db/migration`
  - `V1__Create_Table_Exchange.sql`
  - `V2__Populate_Table_Exchange.sql`

---

## 📁 Estrutura

```
secao-14-usando-zipkin-distributed-tracing-server/
├── naming-server/      # Eureka Server (+ docker-compose.yml da stack)
├── api-gateway/        # Spring Cloud Gateway
├── book-service/       # Microsserviço de livros (Feign → exchange-service)
└── exchange-service/   # Microsserviço de câmbio
```

O `docker-compose.yml` (em `naming-server/`) define os serviços, a rede `microservices-app`
e os volumes das bases MySQL. Os serviços se enxergam pelos nomes DNS internos:
`naming-server`, `zipkin-server`, `book-db`, `exchange-db`.

