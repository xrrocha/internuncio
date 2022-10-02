package shuar.mapper.record;

import shuar.Shuar.Mapper;
import shuar.util.Extractor;
import shuar.util.Formatter;

import java.util.List;
import java.util.stream.Collectors;

public class FixedRecordMapper<I> implements Mapper<I, String> {

    private final int length;
    private final List<FixedField<I, ?>> fields;

    public FixedRecordMapper(int length, List<FixedField<I, ?>> fields) {
        final var invalidFields = fields.stream()
                .filter(field -> field.position + field.length > length)
                .map(Field::getName)
                .collect(Collectors.joining(", "));
        if (!invalidFields.isEmpty()) {
            throw new IllegalArgumentException("Invalid field length/position: %s".formatted(invalidFields));
        }
        this.length = length;
        this.fields = fields;
    }

    @Override
    public String map(I item) {
        final var record = " ".repeat(length).toCharArray();
        fields.forEach(field -> {
            final var string = field.format(item);
            if (string != null) {
                System.arraycopy(string.toCharArray(), 0, record, field.position, field.length);
            }
        });
        return new String(record);
    }

    static public class FixedField<I, T> extends Field<I, T> {
        private final int position;
        private final int length;
        private final Alignment alignment;
        private final String padChar;

        private final String nullString;

        public FixedField(String name,
                          int position,
                          int length,
                          Extractor<I, T> extractor,
                          Formatter<T> formatter,
                          Alignment alignment,
                          char padChar) {
            super(name, extractor, formatter);
            this.position = position;
            this.length = length;
            this.alignment = alignment;
            this.padChar = String.valueOf(padChar);
            nullString = this.padChar.repeat(length);
        }

        @Override
        public String format(I item) {
            final var extractedValue = extractor.extract(item);
            if (item == null) {
                return nullString;
            }
            var formattedValue = formatter.format(extractedValue);
            if (formattedValue == null) {
                formattedValue = extractedValue.toString();
            }
            return alignment.align(formattedValue, length, padChar);
        }
    }

    public enum Alignment {
        LEFT, RIGHT;

        String align(String value, int length, String padding) {
            final String str;
            if (value.length() <= length) str = value;
            else str = value.substring(0, length);

            final String result;
            final var paddingLength = length - str.length();
            if (this == LEFT) {
                result = str + padding.repeat(paddingLength);
            } else {
                result = padding.repeat(paddingLength) + str;
            }
            return result;
        }
    }
}
