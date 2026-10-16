package com.redsocial.api.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ejecuta {@code repair()} antes de {@code migrate()}.
 *
 * <p>Flyway aborta el arranque con "checksum mismatch" cuando una migración ya
 * aplicada se corrige (p. ej. V3 se ajustó tras aplicarse). El {@code repair()}
 * re-sincroniza el checksum del historial con el archivo actual (y limpia
 * entradas fallidas de intentos previos) antes de aplicar migraciones pendientes,
 * evitando que la app se quede sin arrancar (502).</p>
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
