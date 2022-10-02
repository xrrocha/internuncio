package shuar.dsl;

import shuar.Shuar.Mapper;
import shuar.mapper.record.DelimitedRecordMapper;
import shuar.mapper.record.Field;
import shuar.util.Extractor;
import shuar.util.Formatter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

// TODO Add more formatters: [Local|Zoned]Date[Time], ...
public interface DelimitedRecordBuilder<I> extends MapperBuilder<I, String> {
    class Config extends DSLConfig {
        String delimiter = "\t";
        Function<String, String> delimiterEscape = s -> s.replace("\t", "\\t");
        final List<Field<?, ?>> fields = new ArrayList<>();

        @Override
        List<Rule> rules() {
            return List.of(
                    new Rule(() -> delimiter != null, "Delimiter must be specified"),
                    new Rule(() -> delimiterEscape != null, "Delimiter escape must be specified"),
                    new Rule(() -> !fields.isEmpty(), "At least one field must specified")
            );
        }

        static Config get(DelimitedRecordBuilder<?> instance) {
            return get(instance, DelimitedRecordBuilder.class, Config::new);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Mapper<I, String> buildMapper() {
        final Config config = Config.get(this).validate();
        if (config.fields.isEmpty()) {
            throw new IllegalStateException("No fields were specified!");
        }
        return new DelimitedRecordMapper<>(
                config.delimiter,
                (List<Field<I, ?>>) ((List) config.fields),
                config.delimiterEscape);
    }

    default void delimiter(String delimiter) {
        Config.get(this).delimiter = delimiter;
    }

    default void delimiterEscape(Function<String, String> delimiterEscape) {
        Config.get(this).delimiterEscape = delimiterEscape;
    }

    default <T> void field(String name,
                           Extractor<I, T> extractor,
                           Formatter<T> formatter) {
        Config.get(this).fields.add(new Field<>(name, extractor, formatter));
    }

    default <T> void field(String name, Extractor<I, T> extractor) {
        Config.get(this).fields.add(new Field<>(name, extractor, Formatter.from(Object::toString)));
    }

    default void dateField(String name,
                           Extractor<I, ? extends Date> extractor,
                           String pattern) {
        final var dateFormat = new SimpleDateFormat(pattern);
        Config.get(this).fields.add(new Field<>(name, extractor, Formatter.from(dateFormat::format)));
    }

    default <N extends Number> void numberField(String name,
                                                Extractor<I, N> extractor,
                                                String pattern) {
        final var decimalFormat = new DecimalFormat(pattern);
        Config.get(this).fields.add(new Field<>(name, extractor, Formatter.from(decimalFormat::format)));
    }

    default <N extends Number> void decimalField(String name,
                                                 Extractor<I, N> extractor,
                                                 String pattern) {
        final var decimalFormat = new DecimalFormat(pattern);
        decimalFormat.setParseBigDecimal(true);
        Config.get(this).fields.add(new Field<>(name, extractor, Formatter.from(decimalFormat::format)));
    }
}
