package ens.edupath.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

// Ce filtre peut être utilisé pour une authentification route-spécifique si nécessaire
// Par défaut, JwtGlobalFilter est utilisé pour l'authentification globale
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Ce filtre est appliqué uniquement aux routes protégées, donc l'authentification est requise
            if (isAuthMissing(request)) {
                return onError(exchange, "Token JWT manquant", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = getAuthHeader(request);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Format de token invalide. Utilisez: Bearer <token>", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            try {
                // Valider le JWT
                Claims claims = validateToken(token);
                
                // Ajouter les informations du token dans les headers pour les microservices
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", claims.getSubject() != null ? claims.getSubject() : "")
                        .header("X-User-Email", claims.get("email", String.class) != null ? claims.get("email", String.class) : "")
                        .header("X-User-Roles", claims.get("roles", String.class) != null ? claims.get("roles", String.class) : "")
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                return onError(exchange, "Token JWT invalide: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String body = String.format("{\"error\": \"%s\", \"status\": %d}", err, httpStatus.value());
        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
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

    public static class Config {
        // Configuration vide pour le moment
    }
}

