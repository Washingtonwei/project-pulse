package team.projectpulse;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton containers shared by all integration tests. Started once per JVM — avoids
 * spinning up a new MySQL/Mailpit container per test class.
 */
public final class SharedContainers {

    public static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

    public static final GenericContainer<?> MAILPIT = new GenericContainer<>(DockerImageName.parse("axllent/mailpit"))
            .withExposedPorts(1025, 8025);

    static {
        MYSQL.start();
        MAILPIT.start();
    }


    private SharedContainers() {
    }

}
