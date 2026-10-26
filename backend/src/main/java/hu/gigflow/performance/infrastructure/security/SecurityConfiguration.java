package hu.gigflow.performance.infrastructure.security;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

    private static final String API_PATH = "/api/**";

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Environment environment,
            ObjectMapper objectMapper,
            @Value("${gigflow.security.demo-csrf-disabled:false}") boolean demoCsrfDisabled) throws Exception {
        AuthenticationEntryPoint authenticationEntryPoint = (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"GigFlow\"");
            writeProblem(objectMapper, response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized", "Érvényes bejelentkezés szükséges.", request.getRequestURI());
        };

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/session").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/performances").hasAnyRole("MEMBER", "LEADER")
                        .requestMatchers(HttpMethod.POST, "/api/performances").hasRole("LEADER")
                        .requestMatchers(HttpMethod.PUT, "/api/performances/*/response").hasRole("MEMBER")
                        .requestMatchers(API_PATH).authenticated()
                        .anyRequest().denyAll())
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .requestCache(cache -> cache.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler((request, response, exception) -> writeProblem(
                                objectMapper, response, HttpServletResponse.SC_FORBIDDEN,
                                "Forbidden", "Ehhez a művelethez nincs jogosultságod.", request.getRequestURI())));

        if (demoCsrfDisabled && environment.acceptsProfiles(Profiles.of("local", "demo"))) {
            http.csrf(csrf -> csrf.ignoringRequestMatchers(API_PATH));
        }

        return http.build();
    }

    private static void writeProblem(
            ObjectMapper objectMapper,
            HttpServletResponse response,
            int status,
            String title,
            String detail,
            String instance) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "type", "about:blank",
                "title", title,
                "status", status,
                "detail", detail,
                "instance", instance));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @Profile("!local & !demo")
    UserDetailsService noApplicationUsersOutsideDemoProfiles() {
        return username -> {
            throw new UsernameNotFoundException("Nincs konfigurált alkalmazásfelhasználó.");
        };
    }
}
