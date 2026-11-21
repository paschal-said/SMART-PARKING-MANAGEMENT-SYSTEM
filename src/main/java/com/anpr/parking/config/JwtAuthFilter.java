package com.anpr.parking.config;

import com.anpr.parking.user.User;
import com.anpr.parking.user.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserRepository users;

    public JwtAuthFilter(JwtService jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Claims claims = jwt.parse(header.substring(7));
            if (claims != null) {
                try {
                    Long id = Long.valueOf(claims.getSubject());
                    User u = users.findById(id).orElse(null);
                    if (u != null && u.isActive()) {
                        AuthUser principal = new AuthUser(u.getId(), u.getEmail(), u.getRole());
                        var auth = new UsernamePasswordAuthenticationToken(principal, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        chain.doFilter(req, res);
    }
}
