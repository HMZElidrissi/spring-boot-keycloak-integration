# Spring Boot Keycloak Integration
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Keycloak](https://img.shields.io/badge/Keycloak-26.0.7-blue.svg)](https://www.keycloak.org/)

This project demonstrates the integration of Keycloak with a Spring Boot application for secure authentication and authorization.

## Prerequisites

- Java 17
- Maven 3.8+
- Docker

## Quick Start

### 1. Start Keycloak

```bash
docker run -p 8087:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.0.7 start-dev
```

### 2. Configure Keycloak

1. Access Keycloak Admin Console: http://localhost:8087
2. Login with admin/admin
3. Create a new realm:
    - Name: `banking-management-system`

4. Create client:
    - Clients → Create client
    - Client ID: `banking-management-system`
    - Client Protocol: `openid-connect`
    - Client authentication: `ON`
    - Configure redirect URIs: `http://localhost:3000/*`
    - Web Origins: `http://localhost:3000`

5. Create roles:
    - Realm roles → Create role
    - Create: `ADMIN`, `USER`, `EMPLOYEE`

6. Create test users:
   ```
   username: admin
   password: admin
   role: ADMIN
   
   username: user
   password: user
   role: USER
   
   username: employee
   password: employee
   role: EMPLOYEE
   ```

### 3. Build and Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Configuration

### application.yml
    
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8087/realms/bank-management-system
          jwk-set-uri: http://localhost:8087/realms/bank-management-system/protocol/openid-connect/certs
          
    application:
      name: spring-boot-keycloak-integration
      
server:
  port: 8443
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: yourpass
    key-store-type: PKCS12
    key-alias: banking
    enabled: true

keycloak:
  realm: bank-management-system
  auth-server-url: http://localhost:8087
  ssl-required: external
  resource: bank-management-system
  credentials:
    secret: LnQ4Q2mlY6Yq140E7QMlz8BQzkg2j7Tq # Client Secret
  use-resource-role-mappings: true
```

## Testing the Endpoints

1. Get Access Token:
```bash
curl -X POST http://localhost:8087/realms/banking/protocol/openid-connect/token \
  -H 'content-type: application/x-www-form-urlencoded' \
  -d 'client_id=banking-app&username=admin&password=admin&grant_type=password&client_secret=YOUR_CLIENT_SECRET'
```
Replace `YOUR_CLIENT_SECRET` with the client secret from Keycloak (found in Client → Credentials).

2. Test Protected Endpoints:
```bash
# Admin endpoint
curl http://localhost:8443/api/admin/test \
  -H 'Authorization: Bearer YOUR_TOKEN'

# User endpoint
curl http://localhost:8443/api/user/test \
  -H 'Authorization: Bearer YOUR_TOKEN'

# Employee endpoint
curl http://localhost:8443/api/employee/test \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

## SSL Configuration

### 1. Generate SSL Certificate

First, generate a self-signed SSL certificate using the Java keytool:

```bash
keytool -genkeypair \
  -alias banking \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -validity 365 \
  -dname "CN=localhost, OU=IT, O=banking-app, L=Paris, S=IDF, C=FR"

Je vais ajouter une section SSL complète à votre README.md. Voici la nouvelle section à insérer avant la section "Testing the Endpoints" :

```markdown
## SSL Configuration

### 1. Generate SSL Certificate

First, generate a self-signed SSL certificate using the Java keytool:

```bash
keytool -genkeypair \
  -alias banking \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -validity 365 \
  -dname "CN=localhost, OU=IT, O=banking-app, L=Paris, S=IDF, C=FR"
```

When prompted, enter a password for the keystore. Remember this password as you'll need it for the application configuration.

### 2. Configure SSL in Spring Boot

Place the generated `keystore.p12` file in `src/main/resources/`.

Update your `application.yml` with SSL configuration:

```yaml
server:
  port: 8443  # Standard HTTPS port
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-type: PKCS12
    key-store-password: yourpass
    key-alias: banking
    key-password: yourpass
```

### 3. SSL Security Headers

Add the following security configuration to enforce HTTPS:

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // ... other configurations ...
            .headers()
                .httpStrictTransportSecurity()
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000) // 1 year
            .and()
                .contentSecurityPolicy("default-src 'self'")
            .and()
                .referrerPolicy(ReferrerPolicy.NO_REFERRER);
    }
}
```

