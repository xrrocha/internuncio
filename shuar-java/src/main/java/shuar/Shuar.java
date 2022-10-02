package shuar;

import shuar.util.Utils;

import java.util.stream.Stream;

public class Shuar<I, O, R> {

    public interface Source<I> {
        Stream<I> open();
    }

    public interface Mapper<I, O> {
        O map(I item);
    }

    public interface Reducer<O, R> {
        R reduce(Stream<O> items);
    }

    private final Source<I> source;
    private final Mapper<I, O> mapper;
    private final Reducer<O, R> reducer;

    public Shuar(Source<I> source, Mapper<I, O> mapper, Reducer<O, R> reducer) {
        this.source = source;
        this.mapper = mapper;
        this.reducer = reducer;
    }

    public R mapReduce() {
        try {
            final var incomingItems = source.open();
            final var outgoingItems = incomingItems.map(mapper::map);
            return reducer.reduce(outgoingItems);
        } finally {
            close();
        }
    }

    private void close() {
        Stream.of(source, mapper, reducer)
                .filter(component -> component instanceof AutoCloseable)
                .map(component -> (AutoCloseable) component)
                .forEach(component -> Utils.runUnchecked(component::close));
    }
}
