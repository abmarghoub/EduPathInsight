package ens.edupath.auth.filter;

import ens.edupath.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            username = jwtService.getUsernameFromToken(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Vérifier si le token est expiré
                if (jwtService.isTokenExpired(jwt)) {
                    logger.warn("Token expiré pour l'utilisateur: " + username);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                logger.info("Chargement UserDetails pour: " + username + " - Rôles: " + userDetails.getAuthorities());
                
                if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Utilisateur authentifié: " + username + " avec rôles: " + userDetails.getAuthorities());
                } else {
                    logger.warn("Token invalide pour l'utilisateur: " + username);
                }
            }
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            logger.warn("Utilisateur non trouvé: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de l'authentification JWT: " + e.getMessage(), e);
        }
        filterChain.doFilter(request, response);
    }
}

