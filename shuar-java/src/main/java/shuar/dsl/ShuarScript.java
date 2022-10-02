package shuar.dsl;

import shuar.Shuar;

public abstract class ShuarScript<I, O, R> implements
        SourceBuilder<I>,
        MapperBuilder<I, O>,
        ReducerBuilder<O, R> {

    public R mapReduce() {
        return new Shuar<>(buildSource(), buildMapper(), buildReducer())
                .mapReduce();
    }
}
