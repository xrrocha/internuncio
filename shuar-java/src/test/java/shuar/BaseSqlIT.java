package shuar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import shuar.util.Utils;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public abstract class BaseSqlIT {

    private static final String jdbcUrl =
            "jdbc:h2:mem:test;" +
                    String.join(";",
                            "DATABASE_TO_LOWER=true",
                            "DEFAULT_NULL_ORDERING=high",
                            "DB_CLOSE_DELAY=-1",
                            "DB_CLOSE_ON_EXIT=false");
    private static final Properties credentials = new Properties();

    static {
        credentials.setProperty("user", "sa");
        credentials.setProperty("password", "sa");
    }

    private static final Driver driver = new org.h2.Driver();

    protected Connection connection;

    protected abstract List<String> resourceNames();

    @BeforeEach
    public void openDatabase() {
        Utils.runUnchecked(() -> {
            connection = driver.connect(jdbcUrl, credentials);
            resourceNames().stream()
                    .flatMap(resourceName -> Utils.getUnchecked(() ->
                            Arrays.stream(Utils.readResource(resourceName).split(";\n"))))
                    .forEach(sql -> Utils.runUnchecked(() -> connection.createStatement().execute(sql)));
        });
    }

    @AfterEach
    public void closeDatabase() {
        Utils.runUnchecked(() -> connection.close());
    }
}
