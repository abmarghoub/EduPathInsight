package ens.edupath.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Listes des chemins publics qui ne nécessitent pas d'authentification
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/",
            "/api/public/",
            "/actuator/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Ignorer les requêtes OPTIONS (preflight CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        // Vérifier si le chemin est public
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Pour les routes protégées, vérifier le token JWT
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Token JWT manquant ou format invalide", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = validateToken(token);
            
            // Ajouter les informations du token dans les headers pour les microservices
            // IMPORTANT: Conserver le header Authorization pour que le service auth-service puisse authentifier
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject() != null ? claims.getSubject() : "")
                    .header("X-User-Email", claims.get("email", String.class) != null ? 
                            String.valueOf(claims.get("email")) : "")
                    .header("X-User-Roles", claims.get("roles", String.class) != null ? 
                            String.valueOf(claims.get("roles")) : "")
                    // Conserver le header Authorization original pour les services qui en ont besoin
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            return onError(exchange, "Token JWT invalide: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String body = String.format("{\"error\": \"%s\", \"status\": %d}", err, httpStatus.value());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private Claims validateToken(String token) {
        // S'assurer que la clé secrète fait au moins 256 bits (32 bytes)
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Étendre la clé en répétant si nécessaire pour garantir 256 bits
            byte[] extendedKey = new byte[32];
            for (int i = 0; i < 32; i++) {
                extendedKey[i] = keyBytes[i % keyBytes.length];
            }
            keyBytes = extendedKey;
        }
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public int getOrder() {
        // Exécuter ce filtre avant les autres filtres de routage
        return -1;
    }
}

