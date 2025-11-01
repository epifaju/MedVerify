package com.medverify.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtre de Rate Limiting pour protéger les endpoints sensibles
 * Utilise un cache en mémoire (ConcurrentHashMap) pour tracker les requêtes par IP
 * 
 * Protection appliquée :
 * - /api/v1/auth/login : 5 tentatives/minute par IP
 * - /api/v1/auth/register : 3 tentatives/minute par IP
 * - /api/v1/auth/verify : 10 tentatives/minute par IP
 * - /api/v1/medications/verify : 100 requêtes/heure par IP (sans authentification)
 */
@Component
@Order(1) // Exécuter avant le filtre JWT
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.login-per-minute:5}")
    private int loginPerMinute;

    @Value("${app.rate-limit.register-per-minute:3}")
    private int registerPerMinute;

    @Value("${app.rate-limit.verify-email-per-minute:10}")
    private int verifyEmailPerMinute;

    @Value("${app.rate-limit.medication-verify-per-hour:100}")
    private int medicationVerifyPerHour;

    @Value("${app.rate-limit.general-per-minute:60}")
    private int generalPerMinute;

    // Cache pour stocker les compteurs de requêtes par IP
    // Structure: Map<Key, RequestInfo>
    private final Map<String, RequestInfo> requestCache = new ConcurrentHashMap<>();

    // Nettoyage automatique toutes les 5 minutes
    private static final long CLEANUP_INTERVAL = 5 * 60 * 1000; // 5 minutes
    private long lastCleanup = System.currentTimeMillis();

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Nettoyage périodique du cache
        cleanupExpiredEntries();

        String path = request.getRequestURI();
        String clientIp = getClientIP(request);
        String key = getCacheKey(clientIp, path);

        // Déterminer la limite selon le chemin
        int limit;
        long windowMs;

        if (path.contains("/api/v1/auth/login")) {
            limit = loginPerMinute;
            windowMs = 60 * 1000; // 1 minute
        } else if (path.contains("/api/v1/auth/register")) {
            limit = registerPerMinute;
            windowMs = 60 * 1000; // 1 minute
        } else if (path.contains("/api/v1/auth/verify") || path.contains("/api/v1/auth/resend-code")) {
            limit = verifyEmailPerMinute;
            windowMs = 60 * 1000; // 1 minute
        } else if (path.contains("/api/v1/medications/verify")) {
            limit = medicationVerifyPerHour;
            windowMs = 60 * 60 * 1000; // 1 heure
        } else {
            // Pour les autres endpoints, utiliser la limite générale
            limit = generalPerMinute;
            windowMs = 60 * 1000; // 1 minute
        }

        RequestInfo info = requestCache.get(key);
        long now = System.currentTimeMillis();

        if (info == null || (now - info.firstRequestTime) > windowMs) {
            // Nouvelle fenêtre de temps, réinitialiser le compteur
            info = new RequestInfo(1, now);
            requestCache.put(key, info);
        } else {
            // Vérifier si la limite est dépassée
            if (info.count >= limit) {
                log.warn("Rate limit exceeded for IP: {} on path: {} ({} requests in window)", 
                        clientIp, path, info.count);
                
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                
                long retryAfter = windowMs - (now - info.firstRequestTime);
                response.setHeader("Retry-After", String.valueOf(retryAfter / 1000));
                
                String errorMessage = String.format(
                    "{\"error\":\"TOO_MANY_REQUESTS\",\"message\":\"Trop de requêtes. Limite: %d/%s. Réessayez dans %d secondes.\"}",
                    limit, 
                    windowMs == 60 * 1000 ? "minute" : "heure",
                    retryAfter / 1000
                );
                
                response.getWriter().write(errorMessage);
                return;
            }
            
            // Incrémenter le compteur
            info.count++;
            requestCache.put(key, info);
        }

        // Ajouter des headers pour informer le client des limites
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(limit - info.count));
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extrait l'IP du client en tenant compte des proxies
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Prendre la première IP de la chaîne
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Génère une clé de cache unique pour IP + path
     */
    private String getCacheKey(String ip, String path) {
        // Pour les endpoints authentifiés, on pourrait ajouter l'user ID si disponible
        return ip + ":" + path;
    }

    /**
     * Nettoie les entrées expirées du cache
     */
    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < CLEANUP_INTERVAL) {
            return;
        }

        // Supprimer les entrées de plus de 2 heures
        long maxAge = 2 * 60 * 60 * 1000;
        requestCache.entrySet().removeIf(entry -> 
            (now - entry.getValue().firstRequestTime) > maxAge
        );

        lastCleanup = now;
        log.debug("Rate limiting cache cleaned. Current size: {}", requestCache.size());
    }

    /**
     * Classe interne pour stocker les informations de requête
     */
    private static class RequestInfo {
        int count;
        long firstRequestTime;

        RequestInfo(int count, long firstRequestTime) {
            this.count = count;
            this.firstRequestTime = firstRequestTime;
        }
    }
}

