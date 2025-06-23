package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "com.example.demo.service")
public class SecurityConfig {

    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${security.restrict-admin-endpoints:true}")
    private boolean restrictAdminEndpoints;

    @Value("${test.value:NOT_SET}")
    private String testValue;

    @PostConstruct
    public void debugProfile() {
        System.out.println("🌍 PROFILE: test.value = " + testValue);
        System.out.println("🔐 restrictAdminEndpoints = " + restrictAdminEndpoints);
    }

    private static final String[] PROTECTED_ENDPOINTS = {
            "/api/user/{id}",
            "/api/user/change-password",
            "/api/apartments/create-reservation",
            "/api/apartments/reservation-on-hold/**"
    };

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/apartments",
            "/api/apartments/available",
            "/api/apartments/{id}",
            "/api/user/register",
            "/api/user/login",
            "/api/user/forgot-password",
            "/api/user/reset-password",
            "/api/s3/list/**",
            "/api/user/confirm",
            "/api/user/profile"
    };

    private static final String[] ADMIN_ENDPOINTS = {
            "/api/admin/**",
            "/api/user/delete/**",
            "/api/user/allUsers",
            "/api/email-templates/create-template",
            "/api/email-templates/get-all-templates",
            "/api/email-templates/get-template-by-key",
            "/api/email-templates/update-template/**",
            "/api/email-templates/delete-template/**",
            "/api/apartments/get-reservations",
            "/api/apartments/delete-reservation/**",
            "/api/apartments/delete-pending",
            "/api/apartments/cancel-reservation/**",
            "/api/apartments/confirm-reservation/**",
            "/api/apartments",
            "/api/apartments/**",
            "/api/s3/upload-multiple",
            "/api/s3/delete-multiple/**"
    };

    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(ADMIN_ENDPOINTS).permitAll()
                        .requestMatchers(PROTECTED_ENDPOINTS).hasAuthority("client")
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(PUBLIC_ENDPOINTS).permitAll();
                    auth.requestMatchers(PROTECTED_ENDPOINTS).hasAuthority("client");

                    if (restrictAdminEndpoints) {
                        auth.requestMatchers(ADMIN_ENDPOINTS).hasAuthority("admin");
                    } else {
                        auth.requestMatchers(ADMIN_ENDPOINTS).permitAll();
                    }

                    auth.anyRequest().permitAll();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        var secretKey = new SecretKeySpec(jwtSecretKey.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(CustomUserDetailsService customUserDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}