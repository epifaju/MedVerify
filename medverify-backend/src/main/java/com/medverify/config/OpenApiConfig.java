package com.medverify.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI / Swagger pour la documentation API
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI medVerifyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MedVerify API")
                        .description(
                                "API Backend pour la vérification d'authenticité de médicaments.\n\n" +
                                "## Authentification\n" +
                                "L'API utilise JWT (JSON Web Tokens) pour l'authentification.\n" +
                                "1. S'inscrire via `/api/v1/auth/register`\n" +
                                "2. Se connecter via `/api/v1/auth/login` pour obtenir un access token\n" +
                                "3. Inclure le token dans le header `Authorization: Bearer {token}` pour les requêtes authentifiées\n\n" +
                                "## Rate Limiting\n" +
                                "Certains endpoints sont soumis à un rate limiting pour protéger l'API contre les abus.\n\n" +
                                "## Codes d'erreur\n" +
                                "- `400` - Bad Request (requête invalide)\n" +
                                "- `401` - Unauthorized (non authentifié)\n" +
                                "- `403` - Forbidden (pas de permissions)\n" +
                                "- `404` - Not Found (ressource introuvable)\n" +
                                "- `409` - Conflict (ressource déjà existante)\n" +
                                "- `429` - Too Many Requests (rate limit dépassé)\n" +
                                "- `500` - Internal Server Error"
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MedVerify Support")
                                .email("support@medverify.gw")
                        )
                        .license(new License()
                                .name("Proprietary")
                                .url("https://medverify.gw")
                        ))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Serveur de développement local"),
                        new Server()
                                .url("https://api.medverify.gw")
                                .description("Serveur de production")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtenu via /api/v1/auth/login")
                        ))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}

