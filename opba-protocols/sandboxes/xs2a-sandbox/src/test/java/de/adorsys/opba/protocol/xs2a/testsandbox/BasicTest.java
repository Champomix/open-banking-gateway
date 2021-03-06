package de.adorsys.opba.protocol.xs2a.testsandbox;

import de.adorsys.opba.protocol.xs2a.testsandbox.internal.SandboxApp;
import de.adorsys.opba.protocol.xs2a.testsandbox.internal.StarterContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

import static de.adorsys.opba.protocol.xs2a.testsandbox.Const.ENABLE_HEAVY_TESTS;
import static de.adorsys.opba.protocol.xs2a.testsandbox.Const.TRUE_BOOL;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.StringContains.containsString;

@Slf4j
@EnabledIfEnvironmentVariable(named = ENABLE_HEAVY_TESTS, matches = TRUE_BOOL)
class BasicTest extends BaseMockitoTest {
    private static final SandboxAppsStarter executor = new SandboxAppsStarter();
    private static StarterContext ctx;

    static void startSandboxWithJars() {
        ctx = executor.runAll();
        executor.awaitForAllStarted();
    }

    @AfterAll
    static void stopSandbox() {
        executor.shutdown();
    }

    /**
     * Sanity test that validates E2E-Sandbox can run on platform.
     */
    @Test
    @SneakyThrows
    void testEnvStartsUp() {
        startSandboxWithJars();

        assertThat(SandboxApp.values()).extracting(it -> ctx.getLoader().get(it)).isNotNull();
        // Dockerized UI can reach backend
        given().when()
            .post("http://localhost:4400/oba-proxy/ais/FOO=BAR/authorisation/12345/login?pin=12345")
            .then()
            .body(containsString("Internal Server Error"))
            .statusCode(500);
    }

    /**
     * Not really a test, but just launches entire old version sandbox for you.
     * If run with intellij 2018.3 please set VM option to -DSTART_SANDBOX_OLD=true and environment-variable ENABLE_HEAVY_TESTS=true
     */
    @Test
    @SneakyThrows
    @EnabledIfSystemProperty(named = "START_SANDBOX_OLD", matches = TRUE_BOOL)
    void startTheSandbox() {
        startSandboxWithJars();

        assertThat(SandboxApp.values()).extracting(it -> ctx.getLoader().get(it)).isNotNull();

        // Loop forever.
        await().forever().until(() -> false);
    }

    /**
     * Not really a test, but just launches entire sandbox for you.
     * If run with intellij 2018.3 please set VM option to -DSTART_SANDBOX=true and environment-variable ENABLE_HEAVY_TESTS=true
     */
    @Test
    @SneakyThrows
    @EnabledIfSystemProperty(named = "START_SANDBOX", matches = TRUE_BOOL)
    void startTheSandboxInDocker() {
        DockerComposeContainer environment = new DockerComposeContainer(new File("./../../../how-to-start-with-project/xs2a-sandbox-only/docker-compose.yml"))
                                                     .withLocalCompose(true)
                                                     .withTailChildContainers(true);
        environment.start();

        // Loop forever.
        await().forever().until(() -> false);
    }

    /**
     * Not really a test, but just launches Wiremock, with Sandbox mocking fixtures for you.
     * If run with intellij 2018.3 please set VM option to -DSTART_SANDBOX=true and environment-variable ENABLE_HEAVY_TESTS=true
     */
    @Test
    @SneakyThrows
    @EnabledIfSystemProperty(named = "START_SANDBOX", matches = TRUE_BOOL)
    void startTheWiremockInDocker() {
        DockerComposeContainer environment = new DockerComposeContainer(new File("./src/main/resources/docker-compose-with-wiremock.yml"))
                                                     .withLocalCompose(true)
                                                     .withTailChildContainers(true);
        environment.start();

        // Loop forever.
        await().forever().until(() -> false);
    }
}
