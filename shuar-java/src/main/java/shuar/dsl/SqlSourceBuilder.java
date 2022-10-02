package shuar.dsl;

import shuar.Shuar.Source;
import shuar.source.sql.SqlSource;
import shuar.util.Extractor;
import shuar.util.Utils.UncheckedConsumer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Properties;

import static shuar.util.Utils.consumeUnchecked;
import static shuar.util.Utils.getUnchecked;

public interface SqlSourceBuilder extends SourceBuilder<ResultSet> {
    class Config extends DSLConfig {
        String driverClass;
        String jdbcUrl;
        String user;
        String password;
        String query;
        Object parameters;

        UncheckedConsumer<Connection> withConnection = null;

        @Override
        List<Rule> rules() {
            return List.of(
                    new Rule(() -> driverClass != null, "Driver class must be specified"),
                    new Rule(() -> jdbcUrl != null, "JDBC URL must be specified"),
                    new Rule(() -> user != null, "User must be specified"),
                    new Rule(() -> password != null, "Password must be specified"),
                    new Rule(() -> query != null, "Query must be specified")
            );
        }

        private static Config get(SqlSourceBuilder instance) {
            return get(instance, SqlSourceBuilder.class, Config::new);
        }
    }

    @Override
    default Source<ResultSet> buildSource() {
        final Config config = Config.get(this);

        final var connection = getUnchecked(() -> {
            final var properties = new Properties();
            properties.setProperty("user", config.user);
            properties.setProperty("password", config.password);
            final var driver = (Driver) Class.forName(config.driverClass).getConstructor().newInstance();
            return driver.connect(config.jdbcUrl, properties);
        });

        if (config.withConnection != null) {
            consumeUnchecked(connection, config.withConnection);
        }

        return new SqlSource(connection, config.query, config.parameters);
    }

    default void dbDriver(String driverClass, String jdbcUrl) {
        final var config = Config.get(this);
        config.driverClass = driverClass;
        config.jdbcUrl = jdbcUrl;
    }

    default void dbCredentials(String user, String password) {
        final var config = Config.get(this);
        config.user = user;
        config.password = password;
    }

    default void query(String query) {
        Config.get(this).query = query;
    }

    default void parameters(Object parameters) {
        Config.get(this).parameters = parameters;
    }

    default void withConnection(UncheckedConsumer<Connection> withConnection) {
        Config.get(this).withConnection = withConnection;
    }

    default Extractor<ResultSet, String> rsString(String columnName) {
        return Extractor.from(rs -> rs.getString(columnName));
    }

    default Extractor<ResultSet, Integer> rsInt(String columnName) {
        return Extractor.from(rs -> ((Number) rs.getObject(columnName)).intValue());
    }

    default Extractor<ResultSet, Long> rsLong(String columnName) {
        return Extractor.from(rs -> ((Number) rs.getObject(columnName)).longValue());
    }

    default Extractor<ResultSet, Double> rsDouble(String columnName) {
        return Extractor.from(rs -> ((Number) rs.getObject(columnName)).doubleValue());
    }

    default Extractor<ResultSet, Float> rsFloat(String columnName) {
        return Extractor.from(rs -> ((Number) rs.getObject(columnName)).floatValue());
    }

    default Extractor<ResultSet, Date> rsDate(String columnName) {
        return Extractor.from(rs -> rs.getDate(columnName));
    }

    // TODO Move Date/LocalDate conversion to utils
    default Extractor<ResultSet, LocalDate> rsLocalDate(String columnName) {
        return Extractor.from(rs ->
                Instant.ofEpochMilli(rs.getDate(columnName).getTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
    }
}
