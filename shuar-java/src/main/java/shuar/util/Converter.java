package shuar.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface Converter<T> extends Parser<T>, Formatter<T> {

    class DateConverter implements Converter<Date> {
        private final DateFormat dateFormat;

        public DateConverter(String pattern) {
            this(new SimpleDateFormat(pattern));
        }

        public DateConverter(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public Date parse(String value) {
            return Utils.getUnchecked(() -> (dateFormat.parse(value)));
        }

        @Override
        public String format(Date value) {
            return dateFormat.format(value);
        }
    }
}
