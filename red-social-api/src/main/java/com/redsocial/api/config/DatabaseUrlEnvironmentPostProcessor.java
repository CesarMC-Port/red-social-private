package com.redsocial.api.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Permite configurar la base de datos con una sola variable {@code DATABASE_URL}
 * (formato Heroku/Cloud: {@code postgresql://user:pass@host:port/db?sslmode=require}),
 * traduciéndola a las propiedades JDBC que espera Spring Boot.
 *
 * <p>Si {@code DATABASE_URL} no está definida, no hace nada y se mantiene el
 * fallback a {@code DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD} de {@code application.yml}.</p>
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String SOURCE_NAME = "databaseUrlEnvironment";
    private static final String JDBC_PREFIX = "jdbc:";
    private static final int DEFAULT_PORT = 5432;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String raw = environment.getProperty("DATABASE_URL");
        if (raw == null || raw.isBlank()) {
            return;
        }

        String parseable = toParseable(raw.trim());
        URI uri;
        try {
            uri = URI.create(parseable);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("DATABASE_URL inválida: " + redact(raw), ex);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("DATABASE_URL sin host: " + redact(raw));
        }

        int port = uri.getPort() > 0 ? uri.getPort() : DEFAULT_PORT;
        String path = uri.getPath();
        String database = (path == null || path.length() <= 1) ? "" : path.substring(1);

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port
                + (database.isEmpty() ? "" : "/" + database);
        String query = uri.getQuery();
        if (query != null && !query.isBlank()) {
            jdbcUrl = jdbcUrl + "?" + query;
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", jdbcUrl);

        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isEmpty()) {
            int separator = userInfo.indexOf(':');
            if (separator >= 0) {
                properties.put("spring.datasource.username", decode(userInfo.substring(0, separator)));
                properties.put("spring.datasource.password", decode(userInfo.substring(separator + 1)));
            } else {
                properties.put("spring.datasource.username", decode(userInfo));
            }
        }

        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, properties));
    }

    private static String toParseable(String raw) {
        String withoutJdbc = raw.startsWith(JDBC_PREFIX) ? raw.substring(JDBC_PREFIX.length()) : raw;
        if (withoutJdbc.startsWith("postgres://")) {
            return "postgresql://" + withoutJdbc.substring("postgres://".length());
        }
        return withoutJdbc;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String redact(String raw) {
        return toParseable(raw).replaceAll("//[^@/]*@", "//***@");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
