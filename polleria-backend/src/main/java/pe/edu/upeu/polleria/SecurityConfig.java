package pe.edu.upeu.polleria;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
public class SecurityConfig {
        private static final Set<String> ROLES = Set.of("ADMINISTRADOR", "CAJERO",
                        "CLIENTE", "OPERACIONES", "MESERO");

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                // The default principal name is the validated JWT subject (String).
                converter.setJwtGrantedAuthoritiesConverter(jwt -> {
                        Map<String, Object> access = jwt.getClaimAsMap("realm_access");
                        Object roles = access == null ? null : access.get("roles");
                        if (!(roles instanceof Collection<?> values))
                                return List.<GrantedAuthority>of();
                        return values.stream().filter(String.class::isInstance).map(String.class::cast)
                                        .filter(ROLES::contains)
                                        .filter(role -> role.equals("CLIENTE") || (Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")) && jwt.getClaimAsString("email") != null && !jwt.getClaimAsString("email").isBlank()))
                                        .distinct()
                                        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                                        .toList();
                });
                return converter;
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter,
                        SecurityErrorHandler errors) throws Exception {
                http.cors(cors -> {
                })
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                                .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable()).logout(
                                                logout -> logout.disable())
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(errors).accessDeniedHandler(
                                                errors))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.GET,"/api/v1/promociones").permitAll()
                                                .requestMatchers("/api/v1/promociones", "/api/v1/promociones/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/destacados").permitAll()
                                                .requestMatchers("/api/v1/destacados", "/api/v1/destacados/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/ubicacion").permitAll()
                                                .requestMatchers("/api/v1/ubicacion", "/api/v1/ubicacion/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/banners").permitAll()
                                                .requestMatchers("/api/v1/banners", "/api/v1/banners/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.POST,"/api/v1/productos/imagenes").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/productos/imagenes/*").permitAll()
                                                .requestMatchers(HttpMethod.POST,"/api/v1/web/sesion","/api/v1/web/sesion/revocar","/api/v1/web/pedidos","/api/v1/web/pedidos/*/clave","/api/v1/web/pedidos/*/recuperar","/api/v1/web/pedidos/*/revocar","/api/v1/web/pedidos/*/pago-prueba").permitAll()
                                                .requestMatchers(HttpMethod.GET,"/api/v1/web/mesas","/api/v1/web/mesas/eventos","/api/v1/web/pedidos/*").permitAll()
                                                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ASYNC,jakarta.servlet.DispatcherType.ERROR).permitAll()
                                                .requestMatchers(HttpMethod.POST,"/api/v1/pedidos/*/pago-prueba").hasAnyRole("ADMINISTRADOR","CAJERO")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/pedidos").hasAnyRole("ADMINISTRADOR","CAJERO")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/eventos").hasAnyRole("ADMINISTRADOR","CAJERO","OPERACIONES","MESERO","CLIENTE")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/mesas").hasAnyRole("ADMINISTRADOR","CAJERO","MESERO","CLIENTE")
                                                .requestMatchers(HttpMethod.PATCH,"/api/v1/mesas/*/ocupar","/api/v1/mesas/*/liberar").hasAnyRole("MESERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/operaciones/pedidos").hasAnyRole("OPERACIONES","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/mesero/pedidos").hasAnyRole("MESERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/caja/me").hasAnyRole("CAJERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.PATCH,"/api/v1/pedidos/*/estado").hasAnyRole("OPERACIONES","MESERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/actuator/health",
                                                                "/actuator/health/**")
                                                .permitAll()
                                                .requestMatchers("/actuator/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/categorias",
                                                                "/api/v1/categorias/*",
                                                                "/api/v1/productos", "/api/v1/productos/*")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/v1/categorias",
                                                                "/api/v1/productos")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/categorias/*",
                                                                "/api/v1/productos/*")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/categorias/*",
                                                                "/api/v1/productos/*")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/clientes/me",
                                                                "/api/v1/pedidos/mios", "/api/v1/pedidos/mios/*")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/clientes/me",
                                                                "/api/v1/pedidos/mios")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/me")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/mios/*/cancelar")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/clientes",
                                                                "/api/v1/clientes/*")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/clientes")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/*")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/*")
                                                .hasRole("ADMINISTRADOR")
                                                // Módulo clinica (examen v7): solo el administrador.
                                                .requestMatchers("/api/v1/clinica/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers("/api/v1/delivery/**").denyAll()
                                                .requestMatchers(HttpMethod.PATCH,"/api/v1/pedidos/*/confirmar-pago").hasAnyRole("ADMINISTRADOR","CAJERO")
                                                // Reports must precede general order matchers.
                                                .requestMatchers("/api/v1/pedidos/reportes/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos", "/api/v1/pedidos/*")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/pedidos")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/estado")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .anyRequest().denyAll())
                                .oauth2ResourceServer(oauth -> oauth.authenticationEntryPoint(errors)
                                                .accessDeniedHandler(errors)
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
                return http.build();
        }
}
