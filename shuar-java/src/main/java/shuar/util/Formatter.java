package shuar.util;

import shuar.util.Utils.UncheckedFunction;

public interface Formatter<T> {
    String format(T value);

    static <I> Formatter<I> from(UncheckedFunction<I, String> function) {
        return item -> {
            try {
                return function.apply(item);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
