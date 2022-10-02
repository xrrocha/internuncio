package shuar.dsl;

import shuar.mapper.record.Field;
import shuar.util.Extractor;
import shuar.util.Formatter;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public interface SqlDelimitedRecordBuilder
        extends SqlSourceBuilder, DelimitedRecordBuilder<ResultSet> {
    default void dateField(String name,
                           String columnName,
                           String pattern) {
        final var dateFormat = new SimpleDateFormat(pattern);
        DelimitedRecordBuilder.Config.get(this).fields.add(new Field<>(
                name,
                Extractor.from((ResultSet rs) -> rs.getDate(columnName)),
                Formatter.from(dateFormat::format)));
    }
}
