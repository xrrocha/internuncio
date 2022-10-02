package shuar.util;

import shuar.util.Utils.UncheckedFunction;

public interface Extractor<I, O> {
    O extract(I item);

    static <I, O> Extractor<I, O> from(UncheckedFunction<I, O> function) {
        return item -> {
            try {
                return function.apply(item);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
