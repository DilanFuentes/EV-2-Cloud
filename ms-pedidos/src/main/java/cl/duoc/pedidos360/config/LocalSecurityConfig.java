package cl.duoc.pedidos360.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** SOLO para pruebas locales (perfil "local"). Nunca se usa en la demo ni en la nube. */
@Configuration
@Profile("local")
public class LocalSecurityConfig {

    @Bean
    public SecurityFilterChain localFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
