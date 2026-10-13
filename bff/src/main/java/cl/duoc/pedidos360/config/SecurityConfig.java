package cl.duoc.pedidos360.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuracion de seguridad del BFF.
 *
 * Esta clase es la responsable de cumplir el indicador:
 * "Configura correctamente el BFF para que ... pueda validar el token recibido con el IDaaS
 * definido y solo permita consumir el endpoint si el token es valido"
 *
 * Se valida:
 *  - Firma del token (contra las llaves publicas / JWKS del tenant de Azure AD)
 *  - Issuer (que el token venga del tenant esperado)
 *  - Audience (que el token haya sido emitido para esta API, no para otra)
 *  - Expiracion / vigencia (lo hace por defecto el decoder de Spring)
 *  - Roles/scopes del token para autorizar por rol
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Ej: https://login.microsoftonline.com/{tenant-id}/v2.0
    @Value("${azure.ad.issuer-uri}")
    private String issuerUri;

    // Client ID (audience) de la app registrada en Azure AD para el BFF/API
    @Value("${azure.ad.audience}")
    private String expectedAudience;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    /**
     * Decoder que descarga las llaves publicas (JWKS) desde el issuer de Azure AD
     * y agrega validadores adicionales de issuer y audience.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new AudienceValidator(expectedAudience);
        OAuth2TokenValidator<Jwt> combined = new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience);

        jwtDecoder.setJwtValidator(combined);
        return jwtDecoder;
    }

    /**
     * Traduce los roles/scopes que vienen dentro del JWT (claim "roles" que configura Azure AD
     * para App Roles, y el claim estandar "scp" para scopes delegados) en GrantedAuthority de
     * Spring Security, para poder usar @PreAuthorize("hasRole(...)") en los controllers.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
        Collection<GrantedAuthority> scopeAuthorities = defaultConverter.convert(jwt);

        List<String> roles = jwt.getClaimAsStringList("roles");
        Stream<GrantedAuthority> roleAuthorities = roles == null ? Stream.empty()
                : roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role));

        return Stream.concat(scopeAuthorities == null ? Stream.empty() : scopeAuthorities.stream(), roleAuthorities)
                .collect(Collectors.toSet());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // En produccion reemplazar "*" por el dominio real donde se sirve el frontend Angular
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4200", "https://*.amazonaws.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
