package shuar.source.sql;

import shuar.Shuar.Source;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static shuar.util.Utils.getUnchecked;

public class SqlSource implements Source<ResultSet> {

    private final Connection connection;
    private final ParsedQuery parsedQuery;

    public SqlSource(Connection connection, String query, Object parameters) {
        this.connection = connection;
        this.parsedQuery = new ParsedQuery(query, parameters);
    }

    @Override
    public Stream<ResultSet> open() {
        return resultSet2Stream(parsedQuery.prepareStatement(connection));
    }

    static Stream<ResultSet> resultSet2Stream(ResultSet resultSet) {
        return getUnchecked(() -> {
            final var iterator = new Iterator<ResultSet>() {
                @Override
                public boolean hasNext() {
                    return getUnchecked(resultSet::next);
                }

                @Override
                public ResultSet next() {
                    return resultSet;
                }
            };
            final var spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
            return StreamSupport.stream(() -> spliterator, 0, false);
        });
    }

    public interface ToSqlParameters {
        Map<String, Object> toSqlParameters();
    }

    record ParsedQuery(String sql, List<String> parameterNames, Map<String, Object> parameterValues) {
        private static final Pattern PARAMETER_REF = Pattern.compile(":[_\\p{IsLatin}][_\\p{IsLatin}\\d]+");

        ParsedQuery(String query, Object parameters) {
            this(PARAMETER_REF.matcher(query), parameters);
        }

        ParsedQuery(Matcher matcher, Object parameters) {
            this(matcher.replaceAll("?"),
                    matcher.reset().results().map(match -> match.group().substring(1)).toList(),
                    parameters);
        }

        ParsedQuery(String sql, List<String> parameterNames, Object parameters) {
            this(sql, parameterNames, toParameterValues(parameters, new HashSet<>(parameterNames)));
        }

        ResultSet prepareStatement(Connection connection) {
            return getUnchecked(() -> {
                final var statement = connection.prepareStatement(sql);
                for (int index = 0; index < parameterNames.size(); index++) {
                    final var parameterValue = parameterValues.get(parameterNames.get(index));
                    statement.setObject(index + 1, parameterValue);
                }
                return statement.executeQuery();
            });
        }

        @SuppressWarnings("unchecked")
        static Map<String, Object> toParameterValues(Object object, Set<String> parameterNames) {
            if (object == null) {
                return emptyMap();
            }
            if (object instanceof ToSqlParameters toSqlParameters) {
                return toSqlParameters.toSqlParameters();
            }
            if (object instanceof Map) {
                return (Map<String, Object>) object;
            }
            return getUnchecked(() -> {
                final var propertyDescriptors = propertyDescriptorsFor(object.getClass());
                return parameterNames.stream()
                        .filter(propertyDescriptors::containsKey)
                        .map(parameterName -> {
                            final var propertyDescriptor = propertyDescriptors.get(parameterName);
                            final var propertyValue =
                                    getUnchecked(() -> propertyDescriptor.getReadMethod().invoke(object));
                            final var parameterValue = mapType(propertyValue);
                            return new AbstractMap.SimpleEntry<>(parameterName, parameterValue);
                        })
                        .collect(toMap(Entry::getKey, Entry::getValue));
            });
        }

        private final static Map<Class<?>, Map<String, PropertyDescriptor>> propertyDescriptorCache = new HashMap<>();

        static Map<String, PropertyDescriptor> propertyDescriptorsFor(Class<?> clazz) {
            return propertyDescriptorCache.computeIfAbsent(clazz, (c) -> getUnchecked(() ->
                    Arrays.stream(Introspector.getBeanInfo(c).getPropertyDescriptors())
                            .collect(toMap(PropertyDescriptor::getName, Function.identity()))));
        }

        @SuppressWarnings("unchecked")
        static Object mapType(Object object) {
            if (object == null) {
                return null;
            }
            final var mapper = typeMappers.get(object.getClass());
            if (mapper == null) {
                return object;
            }
            return ((Function<Object, Object>) mapper).apply(object);
        }

        private static final Map<Class<?>, Function<?, ?>> typeMappers = new HashMap<>();

        static {
            // TODO Move Date/LocalDate conversion to utils
            // TODO Add more type mappers as needed...
            addTypeMapper(LocalDate.class, (LocalDate localDate) ->
                    Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        }

        static void addTypeMapper(Class<?> clazz, Function<?, ?> mapper) {
            if (clazz == null) {
                throw new NullPointerException("Null mapped class");
            }
            if (mapper == null) {
                throw new NullPointerException("Null class mapper");
            }
            typeMappers.put(clazz, mapper);
        }
    }
}
