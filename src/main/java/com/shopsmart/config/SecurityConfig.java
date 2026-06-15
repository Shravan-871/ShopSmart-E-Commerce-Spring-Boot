package com.shopsmart.config;

import com.shopsmart.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .userDetailsService(userDetailsService)
            .csrf(csrf -> csrf.disable())
            .headers(h -> h.frameOptions(f -> f.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/style.css", "/uploads/**", "/h2-console/**",
                        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/products/**", "/api/v1/products/**", "/").authenticated()
                .requestMatchers(HttpMethod.POST,   "/products/**", "/api/v1/products/**", "/ui/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/products/**", "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/products/**", "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/cart/**", "/cart/**", "/api/v1/orders/**", "/orders/**",
                        "/api/v1/coupons/**", "/coupons/**", "/api/v1/wishlist/**", "/wishlist/**").authenticated()
                .requestMatchers("/cart/**", "/orders/**", "/cart-page", "/my-orders", "/my-orders/**",
                        "/admin/**", "/wishlist-page").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .rememberMe(me -> me.key("shopsmart-remember-me").tokenValiditySeconds(86400))
            .httpBasic(basic -> {})
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String accept = request.getHeader("Accept");
                    if (accept != null && accept.contains("application/json")) {
                        response.sendError(401, "Unauthorized");
                    } else {
                        new LoginUrlAuthenticationEntryPoint("/login")
                                .commence(request, response, authException);
                    }
                })
            );

        return http.build();
    }
}
