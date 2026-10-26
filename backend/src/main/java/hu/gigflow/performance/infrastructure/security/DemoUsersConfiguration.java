package hu.gigflow.performance.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
@Profile({"local", "demo"})
public class DemoUsersConfiguration {

    @Bean
    UserDetailsService demoUsers(
            PasswordEncoder passwordEncoder,
            @Value("${gigflow.security.demo-users.anna-password}") String annaPassword,
            @Value("${gigflow.security.demo-users.bence-password}") String bencePassword,
            @Value("${gigflow.security.demo-users.csilla-password}") String csillaPassword,
            @Value("${gigflow.security.demo-users.leader-password}") String leaderPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername("anna")
                        .password(passwordEncoder.encode(requirePassword(annaPassword, "anna")))
                        .roles("MEMBER")
                        .build(),
                User.withUsername("bence")
                        .password(passwordEncoder.encode(requirePassword(bencePassword, "bence")))
                        .roles("MEMBER")
                        .build(),
                User.withUsername("csilla")
                        .password(passwordEncoder.encode(requirePassword(csillaPassword, "csilla")))
                        .roles("MEMBER")
                        .build(),
                User.withUsername("leader")
                        .password(passwordEncoder.encode(requirePassword(leaderPassword, "leader")))
                        .roles("LEADER")
                        .build());
    }

    private String requirePassword(String password, String username) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Hiányzik a(z) " + username + " demo felhasználó jelszava.");
        }
        return password;
    }
}
