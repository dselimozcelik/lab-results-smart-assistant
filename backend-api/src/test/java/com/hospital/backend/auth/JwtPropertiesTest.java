package com.hospital.backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class,
                    ValidationAutoConfiguration.class))
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues("lab.jwt.expiry-minutes=60");

    @Test
    void startupFailsForAShortSigningSecret() {
        contextRunner
                .withPropertyValues("lab.jwt.secret=too-short")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void startupAcceptsASecretWithAtLeastThirtyTwoCharacters() {
        contextRunner
                .withPropertyValues("lab.jwt.secret=private-test-secret-at-least-32-characters")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(JwtProperties.class)
    static class TestConfig {
    }
}
