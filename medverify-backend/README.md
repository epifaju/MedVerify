# MedVerify Backend API

API Backend pour l'application mobile de vérification d'authenticité de médicaments.

## Stack Technique

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Base de données**: PostgreSQL 14+ avec extension PostGIS
- **Sécurité**: Spring Security + JWT
- **Migrations**: Flyway
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Monitoring**: Actuator + Prometheus

## Prérequis

- JDK 17+
- Maven 3.8+
- PostgreSQL 14+ avec PostGIS
- Docker (optionnel)

## Installation

### 1. Base de données PostgreSQL

```bash
# Créer la base de données
createdb medverify

# Activer l'extension PostGIS
psql -d medverify -c "CREATE EXTENSION IF NOT EXISTS postgis;"
```

### 2. Configuration

Copier `.env.example` vers `.env` et configurer les variables:

```bash
cp .env.example .env
```

### 3. Build & Run

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Ou avec profil dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

L'API sera accessible sur `http://localhost:8080`

## Documentation API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## Endpoints Actuator

- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

## Tests

```bash
# Run tous les tests
mvn test

# Run avec coverage
mvn clean test jacoco:report
```

## Structure du projet

```
src/main/java/com/medverify/
├── config/              # Configurations (Security, CORS, etc.)
├── controller/          # REST Controllers
├── service/             # Business Logic
├── repository/          # Data Access Layer
├── entity/              # JPA Entities
├── dto/                 # Data Transfer Objects
├── security/            # JWT & Security components
├── exception/           # Exception Handlers
└── util/                # Utilitaires
```

## Migrations Flyway

Les migrations sont dans `src/main/resources/db/migration/`:

- `V1__initial_schema.sql` - Schema initial (users, refresh_tokens)
- `V2__medications_schema.sql` - Médicaments et scans
- `V3__reports_schema.sql` - Signalements

## Variables d'environnement

Voir `.env.example` pour la liste complète des variables configurables.

## Profils Spring

- **dev**: Développement (logs DEBUG, SQL visible)
- **test**: Tests (base H2 en mémoire)
- **prod**: Production (logs optimisés, sécurité renforcée)

## Licence

MIT

