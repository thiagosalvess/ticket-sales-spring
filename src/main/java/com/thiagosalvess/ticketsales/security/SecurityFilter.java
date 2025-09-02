package com.thiagosalvess.ticketsales.security;

import com.thiagosalvess.ticketsales.features.authentication.service.TokenService;
import com.thiagosalvess.ticketsales.features.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public SecurityFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = retrieverToken(request);
        if (token != null) {
            try {
                String email = tokenService.getSubject(token);
                Long userId = tokenService.getClaim(token, "id", Long.class);
                var principal = new UserPrincipal(userId, email, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
            }
        }
        chain.doFilter(request, response);
    }

    private String retrieverToken(HttpServletRequest request) {
        String h = request.getHeader("Authorization");
        if (h == null) return null;
        if (!h.startsWith("Bearer ")) return null;
        String token = h.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
