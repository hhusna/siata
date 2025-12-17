package siata.siata.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import siata.siata.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Nonaktifkan CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Gunakan stateless session
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll() // Izinkan endpoint login & register
                        .requestMatchers("/api/auth/pegawai").authenticated() // Endpoint ini butuh user yg login
                        .requestMatchers("/public/**").permitAll() // Izinkan endpoint publik lainnya
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Izinkan endpoint baru untuk dashboard dan laporan
                        .requestMatchers("/api/dashboard/**").permitAll() // Sesuaikan role jika perlu
                        .requestMatchers("/api/laporan/**").permitAll() // Sesuaikan role jika perlu
                        .requestMatchers("/api/data-version").permitAll() // Polling untuk deteksi perubahan data

                        .anyRequest().authenticated() // Wajibkan autentikasi untuk lainnya
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Tambahkan filter JWT

        return http.build();
    }

}