package ma.hmzelidrissi.springbootkeycloakintegration;

import java.util.Collection;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> {
              auth.requestMatchers("/api/admin/**")
                  .hasRole("ADMIN")
                  .requestMatchers("/api/user/**")
                  .hasRole("USER")
                  .requestMatchers("/api/employee/**")
                  .hasRole("EMPLOYEE")
                  .anyRequest()
                  .authenticated();
            })
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();

    // Configure to look for the roles in resource_access.{client-id}.roles
    grantedAuthoritiesConverter.setAuthoritiesClaimName(
        "resource_access.bank-management-system.roles");
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);

          try {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
              Map<String, Object> resource =
                  (Map<String, Object>) resourceAccess.get("bank-management-system");
              if (resource != null) {
                Collection<String> resourceRoles = (Collection<String>) resource.get("roles");
                if (resourceRoles != null) {
                  resourceRoles.stream()
                      .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                      .forEach(authorities::add);
                }
              }
            }
          } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
          }

          return authorities;
        });

    return jwtAuthenticationConverter;
  }
}
