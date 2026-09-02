package br.com.cantina.Cantina.config;

import br.com.cantina.Cantina.config.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.MediaType;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${cantina.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private List<String> origensPermitidas;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origensPermitidas);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                                            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/produtos", "/produtos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/produtos/todos").hasRole("FUNCIONARIO_CANTINA")
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("FUNCIONARIO_CANTINA")

                        .requestMatchers(HttpMethod.POST, "/produtos/resetar-estoque").hasRole("FUNCIONARIO_CANTINA")
                        .requestMatchers(HttpMethod.POST, "/produtos").hasRole("FUNCIONARIO_CANTINA")
                        .requestMatchers(HttpMethod.PUT, "/produtos/**").hasRole("FUNCIONARIO_CANTINA")
                        .requestMatchers(HttpMethod.PATCH, "/produtos/**").hasRole("FUNCIONARIO_CANTINA")
                        .requestMatchers(HttpMethod.DELETE, "/produtos/**").hasRole("FUNCIONARIO_CANTINA")

                        .requestMatchers(HttpMethod.GET, "/pedidos").hasRole("FUNCIONARIO_CANTINA")
                        .requestMatchers(HttpMethod.PATCH, "/pedidos/*/status").hasRole("FUNCIONARIO_CANTINA")
                        .requestMatchers(HttpMethod.PATCH, "/pedidos/*/pagamento").hasRole("FUNCIONARIO_CANTINA")
                        .requestMatchers(HttpMethod.PATCH, "/pedidos/*/cancelar").authenticated()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> escreverErro(response, HttpStatus.UNAUTHORIZED, "Autenticação necessária");
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> escreverErro(response, HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso");
    }

    private void escreverErro(jakarta.servlet.http.HttpServletResponse response, HttpStatus status, String mensagem) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String corpo = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"erro\":\"%s\"}",
                LocalDateTime.now(), status.value(), mensagem);
        response.getWriter().write(corpo);
    }
}
