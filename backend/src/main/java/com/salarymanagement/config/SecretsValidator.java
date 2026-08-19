package com.salarymanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Fails fast if the application is started under the {@code prod} profile while still
 * using the development JWT signing key. A predictable signing key lets anyone mint a
 * valid token for any user, so this is treated as a startup error rather than a warning.
 */
@Component
@Profile("prod")
public class SecretsValidator implements ApplicationListener<ApplicationReadyEvent> {

    static final String DEV_SECRET_MARKER = "dev-only-insecure-secret";

    private final String jwtSecret;

    public SecretsValidator(@Value("${app.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (jwtSecret == null || jwtSecret.contains(DEV_SECRET_MARKER)) {
            throw new IllegalStateException(
                    "Refusing to run with the development JWT secret. Set the JWT_SECRET environment variable.");
        }
        if (jwtSecret.getBytes().length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 bytes to sign HS256 tokens safely.");
        }
    }
}
