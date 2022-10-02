package shuar.mapper.record;

import shuar.Shuar.Mapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DelimitedRecordMapper<I> implements Mapper<I, String> {
    private final String delimiter;
    private final List<Field<I, ?>> fields;
    private final Function<String, String> escapeDelimiter;

    public DelimitedRecordMapper(List<Field<I, ?>> fields) {
        this("\t", fields, value -> value.replace("\t", "\\t"));
    }

    public DelimitedRecordMapper(String delimiter,
                                 List<Field<I, ?>> fields,
                                 Function<String, String> escapeDelimiter) {
        this.delimiter = delimiter;
        this.fields = fields;
        this.escapeDelimiter = escapeDelimiter;
    }

    @Override
    public String map(I item) {
        return fields.stream()
                .map(field -> field.format(item))
                .map(escapeDelimiter)
                .collect(Collectors.joining(delimiter));
    }
}
