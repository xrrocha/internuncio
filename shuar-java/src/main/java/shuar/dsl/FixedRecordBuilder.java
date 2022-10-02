package shuar.dsl;

import shuar.Shuar.Mapper;
import shuar.mapper.record.FixedRecordMapper;
import shuar.mapper.record.FixedRecordMapper.Alignment;
import shuar.mapper.record.FixedRecordMapper.FixedField;
import shuar.util.Extractor;
import shuar.util.Formatter;
import shuar.util.Utils.UncheckedFunction;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static shuar.mapper.record.FixedRecordMapper.Alignment.LEFT;
import static shuar.mapper.record.FixedRecordMapper.Alignment.RIGHT;

public interface FixedRecordBuilder<I> extends MapperBuilder<I, String> {
    class Config extends DSLConfig {
        int length;
        final List<FixedField<?, ?>> fields = new ArrayList<>();

        @Override
        List<Rule> rules() {
            return List.of(
                    new Rule(() -> length > 0, "Record length must be specified"),
                    new Rule(() -> !fields.isEmpty(), "At least one field must specified")
            );
        }

        static Config get(FixedRecordBuilder<?> instance) {
            return get(instance, FixedRecordBuilder.class, Config::new);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Mapper<I, String> buildMapper() {
        final Config config = Config.get(this).validate();
        if (config.fields.isEmpty()) {
            throw new IllegalStateException("No fields were specified!");
        }
        return new FixedRecordMapper<>(config.length, (List<FixedField<I, ?>>) ((List) config.fields));
    }

    default void length(int length) {
        Config.get(this).length = length;
    }

    default <T> void field(String name,
                           int position,
                           int length,
                           Extractor<I, T> extractor,
                           Formatter<T> formatter,
                           Alignment alignment,
                           char padChar) {
        Config.get(this).fields.add(
                new FixedField<>(name, position, length, extractor, formatter, alignment, padChar));
    }

    default <T> void field(String name,
                           int position,
                           int length,
                           UncheckedFunction<I, T> extractor,
                           UncheckedFunction<T, String> formatter,
                           Alignment alignment,
                           char padChar) {
        field(name, position, length, Extractor.from(extractor), Formatter.from(formatter), alignment, padChar);
    }

    default <T> void field(String name,
                           int position,
                           int length,
                           Extractor<I, T> extractor) {
        field(name, position, length, extractor, Formatter.from(Object::toString), LEFT, ' ');
    }

    default void dateField(String name,
                           int position,
                           int length,
                           Extractor<I, ? extends Date> extractor,
                           String pattern) {
        final var dateFormat = new SimpleDateFormat(pattern);
        field(name, position, length, extractor, Formatter.from(dateFormat::format), LEFT, ' ');
    }

    default <N extends Number> void numberField(String name,
                                                int position,
                                                int length,
                                                Extractor<I, N> extractor,
                                                String pattern) {
        final var decimalFormat = new DecimalFormat(pattern);
        field(name, position, length, extractor, Formatter.from(decimalFormat::format), RIGHT, '0');
    }

    default <N extends Number> void decimalField(String name,
                                                 int position,
                                                 int length,
                                                 Extractor<I, N> extractor,
                                                 String pattern) {
        final var decimalFormat = new DecimalFormat(pattern);
        decimalFormat.setParseBigDecimal(true);
        field(name, position, length, extractor, Formatter.from(decimalFormat::format), RIGHT, '0');
    }
}
