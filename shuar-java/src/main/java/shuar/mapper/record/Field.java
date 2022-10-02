package shuar.mapper.record;

import shuar.util.Extractor;
import shuar.util.Formatter;
import shuar.util.Utils.UncheckedFunction;

// TODO Use interfaces instead of lambdas for extractor and formatter?
public class Field<I, T> {
    private final String name;
    protected final Extractor<I, T> extractor;
    protected final Formatter<T> formatter;

    public Field(String name, Extractor<I, T> extractor, Formatter<T> formatter) {
        this.name = name;
        this.extractor = extractor;
        this.formatter = formatter;
    }

    public Field(String name, UncheckedFunction<I, T> extractor) {
        this.name = name;
        this.extractor = Extractor.from(extractor);
        this.formatter = t -> {
            if (t == null) {
                return null;
            } else {
                return t.toString();
            }
        };
    }

    public Field(String name, UncheckedFunction<I, T> extractor, UncheckedFunction<T, String> formatter) {
        this.name = name;
        this.extractor = Extractor.from(extractor);
        this.formatter = Formatter.from(formatter);
    }

    public String format(I item) {
        final var value = extractor.extract(item);
        if (value == null) {
            return null;
        }
        return formatter.format(value);
    }

    public String getName() {
        return name;
    }
}
