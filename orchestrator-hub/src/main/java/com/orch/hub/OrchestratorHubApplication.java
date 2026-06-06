package com.orch.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.orch.hub")
public class OrchestratorHubApplication {

    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(OrchestratorHubApplication.class, args);
    }

    /**
     * Loads key=value pairs from {@code .env} in the working directory into
     * system properties, so {@code ${VAR}} placeholders in application.yml
     * resolve correctly during local development.
     *
     * <p>Silently ignores missing .env, comments ({@code #}), blank lines,
     * and malformed entries. Never overrides an existing system property.</p>
     */
    private static void loadEnvFile() {
        try {
            var path = java.nio.file.Path.of(".env");
            if (!java.nio.file.Files.exists(path)) {
                return;
            }
            var lines = java.nio.file.Files.readAllLines(path);
            for (var line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq == -1) {
                    continue;
                }
                var key = line.substring(0, eq).trim();
                var value = line.substring(eq + 1).trim();
                if (System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (java.io.IOException ignored) {
            // .env doesn't exist — rely on OS environment variables
        }
    }
}
