package com.ecomaxtienda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.ecomaxtienda.service.CustomUserDetailsService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    
    // Constructor manual en lugar de @RequiredArgsConstructor
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    @SuppressWarnings("deprecation")
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(this.userDetailsService);
        authProvider.setPasswordEncoder(this.passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/imagenes/**")
                .ignoringRequestMatchers("/api/data/**")
                .ignoringRequestMatchers("/admin/upload-profile-image")
                .ignoringRequestMatchers("/client/upload-profile-image")
                .ignoringRequestMatchers("/admin/productos/bulk-upload")
                .ignoringRequestMatchers("/admin/productos/cargar-masivo")
            )
            .authenticationProvider(this.authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/css/**", "/js/**", "/img/**", "/fonts/**", "/").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                // Catálogo público - TODOS LOS ENDPOINTS DE CATÁLOGO
                .requestMatchers("/client/catalogo/**", "/client/catalogo", "/client/catalogo-*", "/client/catalogo-final", "/client/catalogo-integrado", "/client/catalogo-simple-test", "/client/producto-detalle/**", "/client/test-catalogo").permitAll()
                .requestMatchers("/client/home").permitAll() // Home público
                .requestMatchers("/client/agregar-al-carrito", "/client/carrito/**").permitAll() // Carrito público
                .requestMatchers("/api/imagenes/**").permitAll() // Imágenes públicas
                .requestMatchers("/images/**").permitAll() // Imágenes de productos públicas
                .requestMatchers("/api/data/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                // Nota: removimos .requestMatchers("/client/**").hasRole("CLIENTE") para evitar conflictos
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(this.myAuthenticationSuccessHandler())
                .failureUrl("/auth/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            String redirectUrl = "/";
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                redirectUrl = "/admin/portal_administrador";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                redirectUrl = "/client/home";
            }
            response.sendRedirect(redirectUrl);
        };
    }
}
